package top.mrxiaom.overflow.internal.contact

import cn.evolvefield.onebot.sdk.response.contact.LoginInfoResp
import cn.evolvefield.onebot.client.core.Bot
import kotlinx.coroutines.*
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.EventDispatcherImpl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.supervisorJob
import net.mamoe.mirai.utils.*
import org.java_websocket.framing.CloseFrame
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.contact.Updatable
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.data.FallbackFriendGroups
import top.mrxiaom.overflow.internal.data.FriendInfoImpl
import top.mrxiaom.overflow.internal.data.StrangerInfoImpl
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.utils.LoggerInFolder
import top.mrxiaom.overflow.internal.utils.asCoroutineExceptionHandler
import top.mrxiaom.overflow.internal.utils.subLogger
import top.mrxiaom.overflow.internal.utils.update
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@OptIn(MiraiInternalApi::class, LowLevelApi::class)
internal class BotWrapper private constructor(
    private var implBot: Bot,
    defLoginInfo: LoginInfoResp,
    override val configuration: BotConfiguration
) : QQAndroidBot(), RemoteBot, Updatable, CoroutineScope {
    val impl: Bot
        get() = implBot
    override val implGetter: () -> Bot = { impl }
    override val appName: String
        get() = implBot.appName
    override val appVersion: String
        get() = implBot.appVersion
    override val noPlatform: Boolean
        get() = implBot.config.noPlatform
    private var loginInfo: LoginInfoResp = defLoginInfo
    private var friendsInternal: ContactList<FriendWrapper> = ContactList()
    private var groupsInternal: ContactList<GroupWrapper> = ContactList()
    private var otherClientsInternal: ContactList<OtherClientWrapper>? = null
    private var strangersInternal: ContactList<StrangerWrapper> = ContactList()


    suspend fun updateLoginInfo() {
        loginInfo = impl.getLoginInfo().data ?: throw IllegalStateException("刷新机器人信息失败")
    }
    suspend fun updateContacts() {
        val friendsList = impl.getFriendList().data?.map {
            FriendWrapper(this, it)
        }
        if (friendsList != null) {
            friendsInternal.update(friendsList) { impl = it.impl }
            logger.verbose("${friends.size} friends loaded.")
        } else {
            logger.warning("Can not fetch friends list.")
        }

        val groupsList = impl.getGroupList().data?.map {
            GroupWrapper(this, it)
        }
        if (groupsList != null) {
            groupsInternal.update(groupsList) { impl = it.impl }
            logger.verbose("${groups.size} groups loaded.")
        } else {
            logger.warning("Can not fetch groups list.")
        }
    }

    override suspend fun queryUpdate() {
        updateLoginInfo()
        updateContacts()
    }
    suspend fun updateOtherClients(): ContactList<OtherClientWrapper> {
        return (otherClientsInternal ?: ContactList()).apply {
            runCatching {
                update(Mirai.getOnlineOtherClientsList(this@BotWrapper).map {
                    OtherClientWrapper(this@BotWrapper, it)
                }) { info = it.info }
            }
            otherClientsInternal = this
        }
    }
    internal fun updateGroup(group: GroupWrapper): GroupWrapper {
        return ((groups[group.id] as? GroupWrapper) ?: group.also { groupsInternal.delegate.add(it) }). apply {
            impl = group.impl
        }
    }
    internal fun updateFriend(friend: FriendWrapper): FriendWrapper {
        return ((friends[friend.id] as? FriendWrapper) ?: friend.also { friendsInternal.delegate.add(it) }).apply {
            impl = friend.impl
        }
    }
    internal fun updateStranger(stranger: StrangerWrapper): StrangerWrapper {
        return ((strangers[stranger.id] as? StrangerWrapper) ?: stranger.also { strangersInternal.delegate.add(it) }).apply {
            impl = stranger.impl
        }
    }
    @JvmBlockingBridge
    override suspend fun getMsg(messageId: Int): MessageChain? {
        val bot = (net.mamoe.mirai.Bot.instances.firstOrNull() as? BotWrapper) ?: return null
        val data = bot.impl.getMsg(messageId).data ?: return null
        if (data.message.isEmpty()) return null
        return OnebotMessages.deserializeFromOneBot(bot, data.message)
    }
    override val id: Long
        get() = loginInfo.userId

    override val logger: MiraiLogger = configuration.botLoggerSupplier(this)
    internal val networkLogger: MiraiLogger by lazy { configuration.networkLoggerSupplier(this) }
    override val coroutineContext: CoroutineContext =
        CoroutineName("Bot.$id")
            .plus(logger.asCoroutineExceptionHandler())
            .childScopeContext(configuration.parentCoroutineContext)
            .apply {
                job.invokeOnCompletion { throwable ->
                    logger.info { "Bot cancelled" + throwable?.message?.let { ": $it" }.orEmpty() }

                    kotlin.runCatching {
                        val bot = bot
                        if (bot is BotWrapper && bot.impl.channel.isOpen) {
                            bot.close()
                        }
                    }.onFailure {
                        if (it !is CancellationException) logger.error(it)
                    }

                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    net.mamoe.mirai.Bot._instances.remove(id)

                    // help GC release instances
                    groups.forEach { it.members.delegate.clear() }
                    groups.delegate.clear() // job is cancelled, so child jobs are to be cancelled
                    friends.delegate.clear()
                    strangers.delegate.clear()
                }
            }
    override val eventChannel: EventChannel<BotEvent> =
        GlobalEventChannel.filterIsInstance<BotEvent>().filter { it.bot === this@BotWrapper }
    val eventDispatcher: EventDispatcher = EventDispatcherImpl(coroutineContext, logger.subLogger("EventDispatcher"))

    override val isOnline: Boolean
        get() = impl.channel.isOpen
    override val nick: String
        get() = loginInfo.nickname

    override val asFriend: FriendWrapper by lazy {
        Mirai.newFriend(this, FriendInfoImpl(id, nick, "", 0)).cast()
    }
    override val asStranger: StrangerWrapper by lazy {
        Mirai.newStranger(this, StrangerInfoImpl(id, bot.nick)).cast()
    }

    override val friendGroups: FallbackFriendGroups = FallbackFriendGroups(this)

    override val friends: ContactList<Friend>
        get() = friendsInternal
    override val groups: ContactList<Group>
        get() = groupsInternal
    override val otherClients: ContactList<OtherClient>
        get() = otherClientsInternal ?: runBlocking {
            updateOtherClients()
        }
    override val strangers: ContactList<Stranger>
        get() = strangersInternal // TODO: Onebot 未提供陌生人列表接口

    override fun close(cause: Throwable?) {
        if (isActive) {
            if (cause == null) {
                supervisorJob.cancel()
            } else {
                supervisorJob.cancel(CancellationException("Bot closed", cause))
            }
        }
        if (impl.channel.isOpen && !impl.channel.isClosing && !impl.channel.isClosed) {
            impl.channel.close(CloseFrame.NORMAL, "主动关闭")
        }
    }

    override suspend fun login() {
        logger.warning("Bot 已由 OneBot 进行管理，溢出核心不会进行登录操作")
    }

    @JvmBlockingBridge
    override suspend fun executeAction(actionPath: String, params: String?): String {
        return impl.customRequest(actionPath, params).toString()
    }

    override fun sendRawWebSocketMessage(message: String) {
        implBot.channel.send(message)
    }

    override fun sendRawWebSocketMessage(message: ByteArray) {
        implBot.channel.send(message)
    }

    override fun toString(): String = "Bot($id)"

    companion object {
        internal suspend fun wrap(impl: Bot, botConfiguration: BotConfiguration? = null): BotWrapper {
            // also refresh bot id
            val loginInfo = impl.getLoginInfo().data ?: throw IllegalStateException("无法获取机器人账号信息")
            return (net.mamoe.mirai.Bot.getInstanceOrNull(impl.id) as? BotWrapper)?.apply {
                implBot = impl
                updateContacts()
            } ?: BotWrapper(impl, loginInfo, botConfiguration ?: BotConfiguration {
                workingDir = File("bots/${impl.id}")
                if (Overflow.instance.miraiConsole) {
                    botLoggerSupplier = {
                        LoggerInFolder(
                            net.mamoe.mirai.Bot::class,
                            "Bot.${it.id}",
                            workingDir.resolve("logs"),
                            1.weeksToMillis
                        )
                    }
                    networkLoggerSupplier = {
                        LoggerInFolder(
                            net.mamoe.mirai.Bot::class,
                            "Net.${it.id}",
                            workingDir.resolve("logs"),
                            1.weeksToMillis
                        )
                    }
                } else {
                    botLoggerSupplier = { MiraiLogger.Factory.create(net.mamoe.mirai.Bot::class, "Bot.${it.id}") }
                    networkLoggerSupplier = { MiraiLogger.Factory.create(net.mamoe.mirai.Bot::class, "Net.${it.id}") }
                }
            }).apply {
                updateContacts()

                //updateOtherClients()
                @Suppress("INVISIBLE_MEMBER")
                net.mamoe.mirai.Bot._instances[id] = this
            }
        }
        internal suspend fun Bot.wrap(): BotWrapper {
            return wrap(this)
        }
    }
}

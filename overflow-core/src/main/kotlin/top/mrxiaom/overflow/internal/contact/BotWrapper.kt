package top.mrxiaom.overflow.internal.contact

import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.response.contact.LoginInfoResp
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import cn.evolvefield.onebot.sdk.util.data
import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.sdk.util.jsonArray
import cn.evolvefield.onebot.sdk.util.jsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
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
import top.mrxiaom.overflow.action.ActionContext
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.contact.RemoteUser
import top.mrxiaom.overflow.contact.Updatable
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.data.FallbackFriendGroups
import top.mrxiaom.overflow.internal.data.FriendInfoImpl
import top.mrxiaom.overflow.internal.data.StrangerInfoImpl
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.OnebotMessages.findForwardMessage
import top.mrxiaom.overflow.internal.utils.*
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@Suppress("MemberVisibilityCanBePrivate")
internal class BotWrapper private constructor(
    private var implBot: Bot,
    defLoginInfo: LoginInfoResp,
    defJson: JsonElement,
    override val configuration: BotConfiguration
) : QQAndroidBot(), RemoteUser, RemoteBot, Updatable, CoroutineScope {
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
    private var json: JsonElement = defJson
    private var friendsInternal: ContactList<FriendWrapper> = ContactList()
    private var groupsInternal: ContactList<GroupWrapper> = ContactList()
    private var otherClientsInternal: ContactList<OtherClientWrapper>? = null
    private var strangersInternal: ContactList<StrangerWrapper> = ContactList()
    internal val inviteHandledGroups = mutableSetOf<Long>()

    suspend fun updateLoginInfo() {
        loginInfo = impl.getLoginInfo().data ?: throw IllegalStateException("刷新机器人信息失败")
    }
    suspend fun updateContacts() {
        val friendsData = impl.getFriendList()
        val friendsJson = friendsData.json.data?.jsonArray ?: JsonArray()
        val friendsList = friendsData.data.map { friend ->
            val json = friendsJson.firstOrNull { friendJson ->
                friendJson.jsonObject?.ignorable("user_id", 0L) == friend.userId
            } ?: JsonObject()
            FriendWrapper(this, friend, json)
        }
        friendsInternal.update(friendsList) { impl = it.impl }
        logger.verbose("${friends.size} friends loaded.")

        val groupsData = impl.getGroupList()
        val groupsJson = groupsData.json.data?.jsonArray ?: JsonArray()
        val groupsList = groupsData.data.map { group ->
            val json = groupsJson.firstOrNull { groupJson ->
                groupJson.jsonObject?.ignorable("group_id", 0L) == group.groupId
            } ?: JsonObject()
            GroupWrapper(this, group, json)
        }
        groupsInternal.update(groupsList) { impl = it.impl }
        logger.verbose("${groups.size} groups loaded.")
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
            implJson = group.implJson
        }
    }
    internal fun updateFriend(friend: FriendWrapper): FriendWrapper {
        return ((friends[friend.id] as? FriendWrapper) ?: friend.also { friendsInternal.delegate.add(it) }).apply {
            impl = friend.impl
            implJson = friend.implJson
        }
    }
    internal fun updateStranger(stranger: StrangerWrapper): StrangerWrapper {
        return ((strangers[stranger.id] as? StrangerWrapper) ?: stranger.also { strangersInternal.delegate.add(it) }).apply {
            impl = stranger.impl
            implJson = stranger.implJson
        }
    }
    @JvmBlockingBridge
    override suspend fun getMsg(messageId: Int): MessageChain? {
        val data = impl.getMsg(messageId).data ?: return null
        if (data.message.isEmpty()) return null
        return OnebotMessages.toMiraiMessage(data.isJsonMessage, data.message, this)
    }

    private val avatar: String? by lazy {
        if (appName.lowercase() != "gensokyo") null
        else runBlocking { impl.extGetAvatar(null, id).data }
    }
    override fun avatarUrl(spec: AvatarSpec): String {
        return avatar ?: super.avatarUrl(spec)
    }

    override val onebotData: String
        get() = gson.toJson(json)
    override val id: Long
        get() = loginInfo.userId
    override val bot: BotWrapper get() = this
    override val logger: MiraiLogger = configuration.botLoggerSupplier(this)
    override val coroutineContext: CoroutineContext =
        CoroutineName("Bot.$id")
            .plus(logger.asCoroutineExceptionHandler())
            .childScopeContext(configuration.parentCoroutineContext)
            .apply {
                job.invokeOnCompletion { throwable ->
                    logger.info { "Bot cancelled" + throwable?.message?.let { ": $it" }.orEmpty() }

                    kotlin.runCatching {
                        if (bot.impl.channel.isOpen) {
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

    @JvmBlockingBridge
    override suspend fun executeAction(context: ActionContext, params: String?): String {
        return impl.customRequest(params, context).toString()
    }

    override fun sendRawWebSocketMessage(message: String) {
        implBot.channel.send(message)
    }

    override fun sendRawWebSocketMessage(message: ByteArray) {
        implBot.channel.send(message)
    }

    suspend fun sendMessageCommon(contact: CanSendMessage, messageChain: MessageChain): Pair<IntArray, Throwable?> {
        var throwable: Throwable? = null
        val messageIds = runCatching {
            if (contact !is Contact) {
                throw IllegalArgumentException("'contact' is not implemented 'net.mamoe.mirai.Contact'")
            }
            val forward = messageChain.findForwardMessage()
            if (forward != null) {
                val data = OnebotMessages.sendForwardMessage(contact, forward)
                data.safeMessageIds(this)
            } else {
                val msg = Overflow.instance.serializeMessage(this, messageChain)
                val data = contact.sendToOnebot(msg)
                data.safeMessageIds(this)
            }
        }.onFailure {
            throwable = it
        }.getOrElse { intArrayOf() }
        if (throwable != null) {
            logger.warning(throwable)
        }
        return messageIds to throwable
    }

    override fun toString(): String = "Bot($id)"

    companion object {
        internal suspend fun Bot.wrap(
            configuration: BotConfiguration? = null,
            workingDir: (Long.() -> File)? = null
        ): BotWrapper {
            val newBot = this
            // also refresh bot id
            val result = getLoginInfo()
            val json = result.json.data ?: JsonObject()
            val loginInfo = result.data ?: throw IllegalStateException("无法获取机器人账号信息")
            return (net.mamoe.mirai.Bot.getInstanceOrNull(id) as? BotWrapper)?.apply {
                if (implBot.channel.isOpen) {
                    throw IllegalStateException("一个账号 ($id) 只允许接入一条连接")
                }
                implBot = newBot
                updateContacts()
            } ?: run {
                val botConfiguration = configuration ?: defaultBotConfiguration
                if (workingDir != null) {
                    botConfiguration.workingDir = workingDir.invoke(id)
                }
                BotWrapper(this, loginInfo, json, botConfiguration).apply {
                    updateContacts()

                    //updateOtherClients()
                    @Suppress("INVISIBLE_MEMBER")
                    net.mamoe.mirai.Bot._instances[id] = this
                }
            }
        }

        private val Bot.defaultBotConfiguration: BotConfiguration
            get() = BotConfiguration {
                this.workingDir = File("bots/$id")
                if (Overflow.instance.miraiConsole) {
                    botLoggerSupplier = {
                        LoggerInFolder(
                            net.mamoe.mirai.Bot::class,
                            "Bot.${it.id}",
                            this.workingDir.resolve("logs"),
                            1.weeksToMillis
                        )
                    }
                    networkLoggerSupplier = {
                        LoggerInFolder(
                            net.mamoe.mirai.Bot::class,
                            "Net.${it.id}",
                            this.workingDir.resolve("logs"),
                            1.weeksToMillis
                        )
                    }
                } else {
                    botLoggerSupplier = { MiraiLogger.Factory.create(net.mamoe.mirai.Bot::class, "Bot.${it.id}") }
                    networkLoggerSupplier = { MiraiLogger.Factory.create(net.mamoe.mirai.Bot::class, "Net.${it.id}") }
                }
            }
    }
}

interface CanSendMessage {
    suspend fun sendToOnebot(message: String): MsgId?
}

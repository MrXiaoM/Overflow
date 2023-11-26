package top.mrxiaom.overflow.contact

import cn.evole.onebot.sdk.response.contact.LoginInfoResp
import cn.evolvefield.onebot.client.core.Bot
import kotlinx.coroutines.*
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.EventDispatcherImpl
import net.mamoe.mirai.supervisorJob
import net.mamoe.mirai.utils.*
import org.java_websocket.framing.CloseFrame
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.data.FriendInfoImpl
import top.mrxiaom.overflow.data.StrangerInfoImpl
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@OptIn(MiraiInternalApi::class, LowLevelApi::class)
class BotWrapper private constructor(
    implBot: Bot,
    defLoginInfo: LoginInfoResp,
    botConfiguration: BotConfiguration
) : net.mamoe.mirai.Bot, CoroutineScope {
    private var implInternal = implBot
    val impl: Bot
        get() = implInternal
    private var loginInfo: LoginInfoResp = defLoginInfo
    private var friendsInternal: ContactList<FriendWrapper> = ContactList()
    private var groupsInternal: ContactList<GroupWrapper> = ContactList()
    private var otherClientsInternal: ContactList<OtherClientWrapper> = ContactList()
    private var strangersInternal: ContactList<StrangerWrapper> = ContactList()
    suspend fun updateLoginInfo() {
        loginInfo = impl.getLoginInfo().data
    }
    suspend fun updateContacts() {
        friendsInternal = ContactList(impl.getFriendList().data.map {
            FriendWrapper(this, it)
        }.toMutableList())
        groupsInternal = ContactList(impl.getGroupList().data.map {
            GroupWrapper(this, it)
        }.toMutableList())
    }
    suspend fun updateOtherClients() = runCatching {
        val newList = impl.getOnlineClients(false).data.clients.map {
            OtherClientWrapper(this, it)
        }.toMutableList()
        otherClientsInternal.delegate.removeIf { old ->
            newList.none { old.impl.appId == it.impl.appId }
        }
        newList.removeIf {
            otherClientsInternal.any { old -> old.impl.appId == it.impl.appId }
        }
        otherClientsInternal.delegate.addAll(newList)
    }

    override val id: Long = loginInfo.userId
    override val logger: MiraiLogger = MiraiLogger.Factory.create(this::class, "Bot/$id")
    override val configuration: BotConfiguration = botConfiguration
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
    val eventDispatcher: EventDispatcher = EventDispatcherImpl(coroutineContext, logger.subLogger(""))

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

    override val friendGroups: FriendGroups
        get() = throw NotImplementedError("Onebot 未提供好友分组接口")

    override val friends: ContactList<Friend>
        get() = friendsInternal
    override val groups: ContactList<Group>
        get() = groupsInternal
    override val otherClients: ContactList<OtherClient>
        get() = otherClientsInternal //TODO: Onebot 未提供陌生人列表接口
    override val strangers: ContactList<Stranger>
        get() = strangersInternal //TODO: Onebot 未提供陌生人列表接口

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

    companion object {
        suspend fun wrap(impl: Bot, botConfiguration: BotConfiguration = BotConfiguration { fileBasedDeviceInfo("device.json") }): BotWrapper {
            return (net.mamoe.mirai.Bot.getInstanceOrNull(impl.id) as? BotWrapper)?.apply {
                implInternal = impl
            } ?:
            BotWrapper(impl, impl.getLoginInfo().data, botConfiguration).apply {
                updateContacts()

                //updateOtherClients()
                @Suppress("INVISIBLE_MEMBER")
                net.mamoe.mirai.Bot._instances[id] = this
            }
        }
        suspend fun Bot.wrap(): BotWrapper {
            return wrap(this)
        }
    }
}

@Suppress("INVISIBLE_MEMBER")
fun MiraiLogger.subLogger(name: String): MiraiLogger {
    return net.mamoe.mirai.internal.utils.subLoggerImpl(this, name)
}
fun MiraiLogger.asCoroutineExceptionHandler(
    priority: SimpleLogger.LogPriority = SimpleLogger.LogPriority.ERROR,
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { context, e ->
        call(
            priority,
            context[CoroutineName]?.let { "Exception in coroutine '${it.name}'." } ?: "Exception in unnamed coroutine.",
            e
        )
    }
}
package top.mrxiaom.overflow.contact

import cn.evole.onebot.sdk.response.contact.LoginInfoResp
import cn.evolvefield.onebot.client.core.Bot
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.EventDispatcherImpl
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.cast
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.data.FriendInfoImpl
import top.mrxiaom.overflow.data.StrangerInfoImpl
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class, LowLevelApi::class)
class BotWrapper private constructor(
    implBot: Bot,
    defLoginInfo: LoginInfoResp,
    botConfiguration: BotConfiguration
) : net.mamoe.mirai.Bot {
    private var implInternal = implBot
    val impl: Bot
        get() = implInternal
    private var loginInfo: LoginInfoResp = defLoginInfo
    private var friendsInternal: ContactList<FriendWrapper> = ContactList()
    private var groupsInternal: ContactList<GroupWrapper> = ContactList()
    private var otherClientsInternal: ContactList<OtherClientWrapper> = ContactList()
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
    override val coroutineContext: CoroutineContext = CoroutineName("Bot/$id")
    override val eventChannel: EventChannel<BotEvent> = GlobalEventChannel
        .parentScope(Overflow.instance)
        .context(coroutineContext)
        .filterIsInstance()
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
        get() = throw NotImplementedError("Onebot 未提供陌生人列表接口")
    override val strangers: ContactList<Stranger>
        get() = throw NotImplementedError("Onebot 未提供陌生人列表接口")

    override fun close(cause: Throwable?) {
        if (cause != null) logger.warning(cause)
        impl.channel.close()
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
package top.mrxiaom.overflow.contact

import cn.evole.onebot.sdk.response.contact.LoginInfoResp
import cn.evolvefield.onebot.client.core.Bot
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import top.mrxiaom.overflow.Overflow
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
class BotWrapper private constructor(
    implBot: Bot,
    botConfiguration: BotConfiguration
) : net.mamoe.mirai.Bot {
    private var implInternal = implBot
    val impl: Bot
        get() = implInternal
    private lateinit var loginInfo: LoginInfoResp
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
    suspend fun updateOtherClients() {
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

    override val isOnline: Boolean
        get() = true // TODO
    override val nick: String
        get() = loginInfo.nickname

    override val asFriend: Friend
        get() = TODO("Not yet implemented")
    override val asStranger: Stranger
        get() = TODO("Not yet implemented")

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
    }

    override suspend fun login() {
        logger.warning("Bot 已由 OneBot 进行管理，溢出核心不会进行登录操作")
    }

    companion object {
        suspend fun wrap(impl: Bot, botConfiguration: BotConfiguration = BotConfiguration { fileBasedDeviceInfo("device.json") }): BotWrapper {
            return (net.mamoe.mirai.Bot.getInstanceOrNull(impl.id) as? BotWrapper)?.apply {
                implInternal = impl
            } ?:
            BotWrapper(impl, botConfiguration).apply {
                updateLoginInfo()
                updateContacts()
                updateOtherClients()
                @Suppress("INVISIBLE_MEMBER")
                net.mamoe.mirai.Bot._instances[id] = this
            }
        }
        suspend fun Bot.wrap(): BotWrapper {
            return wrap(this)
        }
    }
}
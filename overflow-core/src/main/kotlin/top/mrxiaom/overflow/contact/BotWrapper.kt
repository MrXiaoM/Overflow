package top.mrxiaom.overflow.contact

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

class BotWrapper(
    val impl: Bot,
    botConfiguration: BotConfiguration
) : net.mamoe.mirai.Bot {
    override val id: Long = impl.loginInfo.data.userId
    override val logger: MiraiLogger = MiraiLogger.Factory.create(this::class, "Bot/$id")
    override val configuration: BotConfiguration = botConfiguration
    override val coroutineContext: CoroutineContext = CoroutineName("Bot/$id")
    override val eventChannel: EventChannel<BotEvent> = GlobalEventChannel
        .parentScope(Overflow().scope)
        .context(coroutineContext)
        .filterIsInstance()

    override val isOnline: Boolean
        get() = true // TODO
    override val nick: String
        get() = impl.loginInfo.data.nickname

    override val asFriend: Friend
        get() = TODO("Not yet implemented")
    override val asStranger: Stranger
        get() = TODO("Not yet implemented")

    override val friendGroups: FriendGroups
        get() = throw NotImplementedError("Onebot 未提供好友分组接口")
    @OptIn(MiraiInternalApi::class)
    override val friends: ContactList<Friend>
        get() {
            return ContactList(impl.friendList.data.map {
                FriendWrapper(this@BotWrapper, it)
            }.toMutableList())
        }
    @OptIn(MiraiInternalApi::class)
    override val groups: ContactList<Group>
        get() {
            return ContactList(impl.groupList.data.map {
                GroupWrapper(this@BotWrapper, it)
            }.toMutableList())
        }
    override val otherClients: ContactList<OtherClient>
        get() = throw NotImplementedError("Onebot 未提供其它客户端列表接口")
    override val strangers: ContactList<Stranger>
        get() = throw NotImplementedError("Onebot 未提供陌生人列表接口")

    override fun close(cause: Throwable?) {
        if (cause != null) logger.warning(cause)
    }

    override suspend fun login() {
        logger.warning("Bot 已由 OneBot 进行管理，溢出核心不会进行登录操作")
    }
}
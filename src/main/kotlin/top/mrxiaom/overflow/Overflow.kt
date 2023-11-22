package top.mrxiaom.overflow

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.connection.ConnectFactory
import cn.evolvefield.onebot.client.handler.EventBus
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.StrangerInfo
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.internal.event.EventChannelToEventDispatcherAdapter
import net.mamoe.mirai.internal.event.InternalEventMechanism
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.FileCacheStrategy
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import top.mrxiaom.overflow.listener.FriendMessageListener
import top.mrxiaom.overflow.listener.GroupMessageListener
import top.mrxiaom.overflow.message.OnebotMessages
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.jvm.jvmName


@OptIn(MiraiExperimentalApi::class)
class Overflow : IMirai {
    val scope = CoroutineScope(EmptyCoroutineContext + CoroutineName("overflow"))
    override val BotFactory: BotFactory
        get() = BotFactoryImpl
    override var FileCacheStrategy: FileCacheStrategy = net.mamoe.mirai.utils.FileCacheStrategy.PlatformDefault

    val config: Config by lazy {
        val text = File("config.json")
        Json.decodeFromString(Config.serializer(), text.readText())
    }

    companion object {
        private lateinit var _instance: Overflow
        val logger = MiraiLogger.Factory.create(Overflow::class, "溢出核心")
        @JvmStatic
        fun setup() {
            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
            _MiraiInstance.set(Overflow())
        }
        @JvmStatic
        @get:JvmName("getInstance")
        val instance: Overflow
            get() = _instance
    }

    init {
        _instance = this
        // 暂定禁止 mirai-console 的终端用户须知，它可能已不适用于 Overflow
        try {
            Class.forName("net.mamoe.mirai.console.enduserreadme.EndUserReadme")
            System.setProperty("mirai.console.skip-end-user-readme", "Overflow v${BuildConstants.VERSION}")
        } catch (ignored: ClassNotFoundException) {
        }
        logger.info("Overflow v${BuildConstants.VERSION} 正在运行")

        OnebotMessages.registerSerializers()

        val blockingQueue = LinkedBlockingQueue<String>() //使用队列传输数据

        val service = ConnectFactory(
            BotConfig(config.wsHost), blockingQueue
        )
        val bot = service.ws.createBot().also { BotFactoryImpl.internalBot = it }
        val dispatchers = EventBus(blockingQueue)

        dispatchers.addListener(FriendMessageListener(bot))
        dispatchers.addListener(GroupMessageListener(bot))
    }

    override suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) {

        TODO("Not yet implemented")
    }

    override suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent) {
        TODO("Not yet implemented")
    }

    @OptIn(InternalEventMechanism::class)
    override suspend fun broadcastEvent(event: Event) {
        logger.info("fired event: " + event::class.jvmName)
        if (event is BotEvent) {
            val bot = event.bot
            // TODO Bot has not implemented
            //if (bot is AbstractBot) {
            //    bot.components[EventDispatcher].broadcast(event)
            //}
        } else {
            EventChannelToEventDispatcherAdapter.instance.broadcastEventImpl(event)
        }
    }

    override fun constructMessageSource(
        botId: Long,
        kind: MessageSourceKind,
        fromId: Long,
        targetId: Long,
        ids: IntArray,
        time: Int,
        internalIds: IntArray,
        originalMessage: MessageChain
    ): OfflineMessageSource {
        TODO("Not yet implemented")

    }

    override fun createFileMessage(id: String, internalId: Int, name: String, size: Long): FileMessage {
        TODO("Not yet implemented")
    }

    override fun createUnsupportedMessage(struct: ByteArray): UnsupportedMessage {
        TODO("Not yet implemented")
    }

    override suspend fun downloadForwardMessage(bot: Bot, resourceId: String): List<ForwardMessage.Node> {
        TODO("Not yet implemented")
    }

    override suspend fun downloadLongMessage(bot: Bot, resourceId: String): MessageChain {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun getGroupVoiceDownloadUrl(bot: Bot, md5: ByteArray, groupId: Long, dstUin: Long): String {
        TODO("Not yet implemented")
    }

    override suspend fun getOnlineOtherClientsList(bot: Bot, mayIncludeSelf: Boolean): List<OtherClientInfo> {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun getRawGroupList(bot: Bot): Sequence<Long> {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun getRawGroupMemberList(
        bot: Bot,
        groupUin: Long,
        groupCode: Long,
        ownerId: Long
    ): Sequence<MemberInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean) {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun muteAnonymousMember(
        bot: Bot,
        anonymousId: String,
        anonymousNick: String,
        groupId: Long,
        seconds: Int
    ) {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override fun newFriend(bot: Bot, friendInfo: FriendInfo): Friend {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override fun newStranger(bot: Bot, strangerInfo: StrangerInfo): Stranger {
        TODO("Not yet implemented")
    }

    override suspend fun queryImageUrl(bot: Bot, image: Image): String {
        TODO("Not yet implemented")
    }

    override suspend fun queryProfile(bot: Bot, targetId: Long): UserProfile {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun recallFriendMessageRaw(
        bot: Bot,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun recallGroupMessageRaw(
        bot: Bot,
        groupCode: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray
    ): Boolean {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun recallGroupTempMessageRaw(
        bot: Bot,
        groupUin: Long,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun recallMessage(bot: Bot, source: MessageSource) {
        TODO("Not yet implemented")
    }

    @MiraiExperimentalApi
    override suspend fun refreshKeys(bot: Bot) {
        // TODO
    }

    override suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean, message: String) {
        TODO("Not yet implemented")
    }

    override suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun solveBotInvitedJoinGroupRequestEvent(
        bot: Bot,
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    ) {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun solveMemberJoinRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        groupId: Long,
        accept: Boolean?,
        blackList: Boolean,
        message: String
    ) {
        TODO("Not yet implemented")
    }

    @LowLevelApi
    override suspend fun solveNewFriendRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        accept: Boolean,
        blackList: Boolean
    ) {
        TODO("Not yet implemented")
    }
}
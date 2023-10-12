package top.mrxiaom.overflow

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.StrangerInfo
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.FileCacheStrategy
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger

fun setup() {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    net.mamoe.mirai._MiraiInstance.set(Overflow())
}

lateinit var instance: Overflow

@OptIn(MiraiExperimentalApi::class)
class Overflow : IMirai {
    val logger = MiraiLogger.Factory.create(this::class, "OverflowImpl")
    override val BotFactory: BotFactory
        get() = BotFactoryImpl
    override var FileCacheStrategy: FileCacheStrategy = net.mamoe.mirai.utils.FileCacheStrategy.PlatformDefault

    init {
        instance = this

        val port = System.getProperty("overflow.port", "11451")
            .toIntOrNull()?.run {
                if (this in 1..65535) this else null
            } ?: error("-Doverflow.port 的值无效")

        // 待测试，我不知道在初始化时 runBlocking 是否正确
        runBlocking {
            logger.info("Overflow v${BuildConstants.VERSION} 正在运行")

            val bossGroup: EventLoopGroup = NioEventLoopGroup()
            val workerGroup: EventLoopGroup = NioEventLoopGroup()
            try {
                val boot = ServerBootstrap()
                boot.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            ch.pipeline().addLast(NetworkHandler())
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

                val future: ChannelFuture = boot.bind(port).sync()

                future.channel().closeFuture().sync()
            } finally {
                workerGroup.shutdownGracefully()
                bossGroup.shutdownGracefully()
            }
            logger.info("已在端口 $port 开启接口服务器")
            logger.info("正在等待连接...")
        }
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

    override suspend fun broadcastEvent(event: Event) {
        TODO("Not yet implemented")
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
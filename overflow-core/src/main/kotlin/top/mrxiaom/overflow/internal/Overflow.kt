package top.mrxiaom.overflow.internal

import cn.evole.onebot.sdk.action.ActionRaw
import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evole.onebot.sdk.response.contact.StrangerInfoResp
import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.connection.ConnectFactory
import cn.evolvefield.onebot.client.handler.EventBus
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.StrangerInfo
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.event.EventChannelToEventDispatcherAdapter
import net.mamoe.mirai.internal.event.InternalEventMechanism
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.mrxiaom.overflow.BuildConstants
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.contact.BotWrapper.Companion.wrap
import top.mrxiaom.overflow.internal.contact.FriendWrapper
import top.mrxiaom.overflow.internal.contact.StrangerWrapper
import top.mrxiaom.overflow.internal.data.UserProfileImpl
import top.mrxiaom.overflow.internal.data.asMirai
import top.mrxiaom.overflow.internal.listener.*
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.data.OfflineMessageSourceImpl
import top.mrxiaom.overflow.internal.message.data.WrappedFileMessage
import top.mrxiaom.overflow.internal.plugin.OverflowCoreAsPlugin
import top.mrxiaom.overflow.internal.utils.wrapAsOtherClientInfo
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

val Bot.asOnebot: BotWrapper
    get() = this as? BotWrapper ?: throw IllegalStateException("Bot 非 Overflow 实现")
fun ActionRaw.check(failMsg: String): Boolean {
    if (retCode != 0) {
        Overflow.logger.warning("$failMsg, status=$status, retCode=$retCode, echo=$echo")
    }
    return retCode == 0
}
@OptIn(MiraiExperimentalApi::class, MiraiInternalApi::class, LowLevelApi::class)
class Overflow : IMirai, CoroutineScope, LowLevelApiAccessor, OverflowAPI {
    override val coroutineContext: CoroutineContext = CoroutineName("overflow")
    override val BotFactory: BotFactory
        get() = BotFactoryImpl
    override var FileCacheStrategy: FileCacheStrategy = net.mamoe.mirai.utils.FileCacheStrategy.PlatformDefault
    internal val newFriendRequestFlagMap = mutableMapOf<Long, String>()
    internal val newMemberJoinRequestFlagMap = mutableMapOf<Long, String>()
    internal val newInviteJoinGroupRequestFlagMap = mutableMapOf<Long, String>()
    private var miraiConsoleFlag: Boolean = false
    val miraiConsole: Boolean
        get() = miraiConsoleFlag
    private val prettyJson = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val json = Json {
        ignoreUnknownKeys = true
    }

    val configFile: File by lazy {
        File(System.getProperty("overflow.config", "overflow.json"))
    }
    val config: Config by lazy {
        var config: Config? = null
        if (configFile.exists()) try {
            config = json.decodeFromString(Config.serializer(), configFile.readText())
        } catch (t: Throwable) {
            val bak = File(configFile.parentFile, "${configFile.name}.old_${System.currentTimeMillis()}.bak")
            configFile.copyTo(bak, true)
            logger.warning("读取配置文件错误，已保存旧文件到 ${bak.name}", t)
        } else {
            logger.info("配置文件不存在，正在创建")
        }
        (config ?: Config()).apply {
            configFile.writeText(prettyJson.encodeToString(Config.serializer(), this))
        }
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
        val instance: Overflow get() = _instance

        @JvmStatic
        val version = "${BuildConstants.VERSION}-${BuildConstants.COMMIT_HASH.chunked(7)[0]}"

        private val isNotExit by lazy {
            !System.getProperty("overflow.not-exit").isNullOrBlank()
        }
    }

    init {
        _instance = this
        // 暂定禁止 mirai-console 的终端用户须知，它可能已不适用于 Overflow
        try {
            Class.forName("net.mamoe.mirai.console.enduserreadme.EndUserReadme")
            System.setProperty("mirai.console.skip-end-user-readme", "true")
            miraiConsoleFlag = true
            injectMiraiConsole()
        } catch (ignored: ClassNotFoundException) {
        }
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    private fun injectMiraiConsole() {
        MiraiConsole.pluginManager // init
        val pluginManager: net.mamoe.mirai.console.internal.plugin.PluginManagerImpl = MiraiConsole.pluginManager.cast()
        if (!pluginManager.resolvedPlugins.contains(OverflowCoreAsPlugin)) {
            pluginManager.resolvedPlugins.add(OverflowCoreAsPlugin)
        }
    }

    @JvmOverloads
    @JvmBlockingBridge
    suspend fun start(printInfo: Boolean = false, logger: Logger = LoggerFactory.getLogger("Onebot")): Boolean {
        val reversed = config.reversedWSPort in 1..65535
        if (printInfo) {
            logger.info("Overflow v$version 正在运行")
            if (reversed) {
                logger.info("在端口 ${config.reversedWSPort} 开启反向 WebSocket 服务端")
            } else {
                logger.info("连接到 WebSocket: ${config.wsHost}")
            }
        }

        val service = ConnectFactory.create(
            BotConfig(
                url = config.wsHost,
                reversedPort = config.reversedWSPort,
                token = config.token,
                isAccessToken = config.token.isNotBlank()
            ), logger
        )
        val dispatchers: EventBus
        val botImpl: cn.evolvefield.onebot.client.core.Bot
        if (reversed) {
            val ws = service.createWebsocketServerAndWaitConnect(this)
            if (ws == null) {
                if (printInfo) {
                    logger.error("未连接到 Onebot")
                    if (!isNotExit) exitProcess(1)
                }
                return false
            }
            dispatchers = ws.first.createEventBus()
            botImpl = ws.second
        } else {
            val ws = service.createWebsocketClient(this)
            if (ws == null) {
                if (printInfo) {
                    logger.error("未连接到 Onebot")
                    if (!isNotExit) exitProcess(1)

                }
                return false
            }
            dispatchers = ws.createEventBus()
            botImpl = ws.createBot().also { BotFactoryImpl.internalBot = it }
        }
        val versionInfo = botImpl.getVersionInfo()
        OnebotMessages.appName = (versionInfo.optJSONObject("data").get("app_name")?.asString ?: "onebot").trim().lowercase()
        if (!botImpl.channel.isOpen) {
            if (printInfo) {
                logger.error("未连接到 Onebot")
                if (!isNotExit) exitProcess(1)
            }
            return false
        }
        if (printInfo) {
            logger.info("服务端版本信息\n${versionInfo.toPrettyString()}")
        }
        val bot = botImpl.wrap()

        dispatchers.addGroupListeners(bot)
        dispatchers.addFriendListeners(bot)
        
        BotOnlineEvent(bot).broadcast()
        return true
    }

    override fun imageFromFile(file: String): Image = OnebotMessages.imageFromFile(file)
    override fun audioFromFile(file: String): Audio = OnebotMessages.audioFromFile(file)
    override fun videoFromFile(file: String): ShortVideo = OnebotMessages.videoFromFile(file)
    override fun serializeMessage(message: Message): String = OnebotMessages.serializeToOneBotJson(message)
    @JvmBlockingBridge
    override suspend fun deserializeMessage(bot: Bot, message: String): MessageChain = OnebotMessages.deserializeFromOneBot(bot, message, null)
    override suspend fun queryProfile(bot: Bot, targetId: Long): UserProfile {
        val data = bot.asOnebot.impl.getUserInfo(targetId, false).data ?: throw IllegalStateException("Can not fetch profile card.")
        // TODO: 不确定 birthday 的单位是毫秒还是秒
        val age = if (data.birthday > 0) ((currentTimeSeconds() - data.birthday) / 365.daysToSeconds).toInt() else 0
        // TODO: 获取性别
        return UserProfileImpl(age, data.mail, 0, data.name, data.level, UserProfile.Sex.UNKNOWN, data.hobbyEntry)
    }
    override suspend fun getOnlineOtherClientsList(bot: Bot, mayIncludeSelf: Boolean): List<OtherClientInfo> {
        val data = bot.asOnebot.impl.getOnlineClients(false).data ?: throw IllegalStateException("Can not fetch online clients.")
        return data.clients.map {
            it.wrapAsOtherClientInfo()
        }
    }
    @LowLevelApi
    override suspend fun getGroupVoiceDownloadUrl(bot: Bot, md5: ByteArray, groupId: Long, dstUin: Long): String {
        TODO("Not yet implemented")
    }
    @MiraiExperimentalApi
    override suspend fun refreshKeys(bot: Bot) {
        // TODO: 2021/4/14 MiraiImpl.refreshKeysNow
    }

    @OptIn(InternalEventMechanism::class)
    override suspend fun broadcastEvent(event: Event) {
        if (event is BotEvent) {
            val bot = event.bot
            if (bot is BotWrapper) {
                bot.eventDispatcher.broadcast(event)
            }
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
        return OfflineMessageSourceImpl(botId, fromId, ids, internalIds, true, originalMessage, targetId, time, kind)
    }

    override fun createFileMessage(id: String, internalId: Int, name: String, size: Long): FileMessage {
        return WrappedFileMessage(id, internalId, name, size)
    }

    override fun createUnsupportedMessage(struct: ByteArray): UnsupportedMessage {
        return UnsupportedMessage(struct)
    }

    override suspend fun downloadForwardMessage(bot: Bot, resourceId: String): List<ForwardMessage.Node> {
        return bot.asOnebot.impl.getForwardMsg(resourceId).data.message.map {
            val msg = OnebotMessages.deserializeFromOneBot(bot, it.message)
            ForwardMessage.Node(it.sender.userId, it.time, it.sender.nickname, msg)
        }
    }

    override suspend fun downloadLongMessage(bot: Bot, resourceId: String): MessageChain {
        throw NotImplementedError("Onebot 未提供长消息下载方法")
    }


    @LowLevelApi
    override suspend fun getRawGroupList(bot: Bot): Sequence<Long> {
        return bot.asOnebot.impl.getGroupList().data.map { it.groupId }.asSequence()
    }

    @LowLevelApi
    override suspend fun getRawGroupMemberList(
        bot: Bot,
        groupUin: Long,
        groupCode: Long,
        ownerId: Long
    ): Sequence<MemberInfo> {
        return bot.asOnebot.impl.getGroupMemberList(groupUin).data.map { it.asMirai }.asSequence()
    }

    @LowLevelApi
    override suspend fun muteAnonymousMember(
        bot: Bot,
        anonymousId: String,
        anonymousNick: String,
        groupId: Long,
        seconds: Int
    ) {
        // TODO: 获取匿名群员列表中的 flag 应当赋值给 anonymousId
        bot.asOnebot.impl.setGroupAnonymousBan(groupId, anonymousId, seconds)
    }

    @LowLevelApi
    override fun newFriend(bot: Bot, friendInfo: FriendInfo): Friend {
        return FriendWrapper(bot.asOnebot, FriendInfoResp(friendInfo.uin, friendInfo.nick, friendInfo.remark))
    }

    @LowLevelApi
    override fun newStranger(bot: Bot, strangerInfo: StrangerInfo): Stranger {
        return StrangerWrapper(bot.asOnebot, StrangerInfoResp(strangerInfo.uin, strangerInfo.nick, "", 0, "", 0, 0))
    }

    override suspend fun queryImageUrl(bot: Bot, image: Image): String {
        // Onebot 没有 imageId 概念，Overflow 使用 imageId 字段来存储 Onebot 中的 file 链接
        return image.imageId
    }

    @LowLevelApi
    override suspend fun recallFriendMessageRaw(
        bot: Bot,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int
    ): Boolean {
        return bot.asOnebot.impl.deleteMsg(messageIds[0]).check("撤回好友消息") // 忽略多消息情况
    }

    @LowLevelApi
    override suspend fun recallGroupMessageRaw(
        bot: Bot,
        groupCode: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray
    ): Boolean {
        return bot.asOnebot.impl.deleteMsg(messageIds[0]).check("撤回群消息") // 忽略多消息情况
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
        return bot.asOnebot.impl.deleteMsg(messageIds[0]).check("撤回讨论组消息") // 忽略多消息情况
    }

    override suspend fun recallMessage(bot: Bot, source: MessageSource) {
        bot.asOnebot.impl.deleteMsg(source.ids[0]).check("撤回消息") // 忽略多消息情况
    }

    override suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean {
        val msg = "[{\"type\":\"touch\",\"data\":{\"id\":${nudge.target.id}}}]"
        if (receiver is Group) {
            bot.asOnebot.impl.sendGroupMsg(receiver.id, msg, false)
        } else {
            bot.asOnebot.impl.sendPrivateMsg(receiver.id, msg, false)
        }
        return true
    }


    //========== Bot Invited Join Group Request 邀请机器人加群请求 START =============
    override suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) {
        solveBotInvitedJoinGroupRequestEvent(event.bot, event.eventId, event.invitorId, event.groupId, true)
    }
    override suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) {
        solveBotInvitedJoinGroupRequestEvent(event.bot, event.eventId, event.invitorId, event.groupId, false)
    }
    @LowLevelApi
    override suspend fun solveBotInvitedJoinGroupRequestEvent(bot: Bot, eventId: Long, invitorId: Long, groupId: Long, accept: Boolean) {
        newInviteJoinGroupRequestFlagMap[eventId]?.also {
            bot.asOnebot.impl.setGroupAddRequest(it, "invite", accept, "")
        }
    }
    //========== Bot Invited Join Group Request 邀请机器人加群请求 END =============


    //========== Member Join Request 加群申请 START =============
    override suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent) {
        solveMemberJoinRequestEvent(event.bot, event.eventId, event.fromId, event.fromNick, event.groupId, accept = true, blackList = false, message = event.message)
    }
    override suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean, message: String) {
        solveMemberJoinRequestEvent(event.bot, event.eventId, event.fromId, event.fromNick, event.groupId, accept = false, blackList = blackList, message = message)
    }
    override suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean) {
        solveMemberJoinRequestEvent(event.bot, event.eventId, event.fromId, event.fromNick, event.groupId, accept = null, blackList = blackList, message = event.message)
    }
    @LowLevelApi
    override suspend fun solveMemberJoinRequestEvent(bot: Bot, eventId: Long, fromId: Long, fromNick: String, groupId: Long, accept: Boolean?, blackList: Boolean, message: String) {
        if (accept == null) {
            // TODO 忽略加群请求
            return
        }
        newMemberJoinRequestFlagMap[eventId]?.also {
            bot.asOnebot.impl.setGroupAddRequest(it, "add", accept, message)
        }
    }
    //========== Member Join Request 加群申请 END =============


    //========== New Friend Request 新好友请求 START =============
    override suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent) {
        solveNewFriendRequestEvent(event.bot, event.eventId, event.fromId, event.fromNick, accept = true, blackList = false)
    }
    override suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean) {
        solveNewFriendRequestEvent(event.bot, event.eventId, event.fromId, event.fromNick, false, blackList)
    }
    @LowLevelApi
    override suspend fun solveNewFriendRequestEvent(bot: Bot, eventId: Long, fromId: Long, fromNick: String, accept: Boolean, blackList: Boolean) {
        newFriendRequestFlagMap[eventId]?.also {
            bot.asOnebot.impl.setFriendAddRequest(it, accept, "")
        }
    }
    //========== New Friend Request 新好友请求 END =============

}
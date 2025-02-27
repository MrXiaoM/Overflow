@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package top.mrxiaom.overflow.internal

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.connection.ConnectFactory
import cn.evolvefield.onebot.client.connection.OneBotProducer
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.sdk.action.ActionRaw
import cn.evolvefield.onebot.sdk.response.contact.FriendInfoResp
import cn.evolvefield.onebot.sdk.util.CQCode
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import com.google.gson.JsonParser
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
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.event.EventChannelToEventDispatcherAdapter
import net.mamoe.mirai.internal.event.InternalEventMechanism
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.mrxiaom.overflow.BuildConstants
import top.mrxiaom.overflow.IBotStarter
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.OverflowAPI.Companion.logger
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.contact.RemoteBot.Companion.asRemoteBot
import top.mrxiaom.overflow.internal.cache.MessageCache
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.contact.BotWrapper.Companion.wrap
import top.mrxiaom.overflow.internal.contact.FriendWrapper
import top.mrxiaom.overflow.internal.contact.StrangerWrapper
import top.mrxiaom.overflow.internal.data.UserProfileImpl
import top.mrxiaom.overflow.internal.data.asMirai
import top.mrxiaom.overflow.internal.data.asOnebot
import top.mrxiaom.overflow.internal.listener.addBotListeners
import top.mrxiaom.overflow.internal.listener.addFriendListeners
import top.mrxiaom.overflow.internal.listener.addGroupListeners
import top.mrxiaom.overflow.internal.listener.addGuildListeners
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.data.*
import top.mrxiaom.overflow.internal.plugin.OverflowCoreAsPlugin
import top.mrxiaom.overflow.internal.utils.wrapAsOtherClientInfo
import top.mrxiaom.overflow.spi.MediaURLService
import top.mrxiaom.overflow.spi.MediaURLService.Companion.queryImageUrl
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

internal val OverflowAPI.scope: CoroutineScope
    get() = this as Overflow
internal val Bot.asOnebot: BotWrapper
    get() = this as? BotWrapper ?: throw IllegalStateException("Bot 非 Overflow 实现")

internal fun ActionRaw.check(failMsg: String): Boolean {
    if (retCode != 0) {
        logger.warning("$failMsg, status=$status, retCode=$retCode, echo=$echo")
    }
    return retCode == 0
}

@OptIn(MiraiExperimentalApi::class, MiraiInternalApi::class, LowLevelApi::class)
class Overflow : IMirai, CoroutineScope, LowLevelApiAccessor, OverflowAPI {
    override val coroutineContext: CoroutineContext = CoroutineName("overflow")
    override val BotFactory: BotFactory
        get() = BotFactoryImpl
    override var FileCacheStrategy: FileCacheStrategy = net.mamoe.mirai.utils.FileCacheStrategy.PlatformDefault
    private val newFriendRequestFlagMap = mutableMapOf<Long, String>()
    private val newMemberJoinRequestFlagMap = mutableMapOf<Long, String>()
    private val newInviteJoinGroupRequestFlagMap = mutableMapOf<Long, String>()
    private var miraiConsoleFlag: Boolean = false
    val startupTime = System.currentTimeMillis()
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
    val defaultJob: Job by lazy {
        val job = if (!miraiConsole) {
            SupervisorJob()
        } else {
            MiraiConsole.job
        }
        return@lazy job.also {
            it.invokeOnCompletion {
                MessageCache.cancelSchedule()
            }
        }
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
            logger.info {
                """
                    配置文件不存在，正在创建
                    
                    =============================================
                    
                      ▌ 初次使用 Overflow，请阅读用户手册:
                      https://mirai.mrxiaom.top/docs/UserManual
                    
                      ▌ 部署各 Onebot 实现并连接到 Overflow 的教程：
                      https://wiki.mrxiaom.top/overflow
                    
                      ▌ 反馈漏洞/提交建议：
                      https://github.com/MrXiaoM/Overflow/issues/new/choose
                    
                    =============================================
                
                """.trimIndent()
            }
        }
        (config ?: Config()).apply {
            configFile.writeText(prettyJson.encodeToString(Config.serializer(), this))
        }
    }

    companion object {
        private var _instance: Overflow? = null

        @JvmStatic
        fun setup() {
            _MiraiInstance.set(Overflow())
        }

        @JvmStatic
        @get:JvmName("getInstance")
        val instance: Overflow get() = _instance!!

        val versionNumber: Int?
            get() = BuildConstants.COMMIT_COUNT

        @JvmField
        val version: String =
            if (versionNumber == null) BuildConstants.VERSION
            else "${BuildConstants.VERSION}.$versionNumber"

        private val isNotExit by lazy {
            !System.getProperty("overflow.not-exit").isNullOrBlank()
        }
    }

    override val version: String = Overflow.version

    init {
        if (_instance != null) throw IllegalStateException("Overflow 被重复实例化")
        _instance = this
        _MiraiInstance.set(this)

        EventBus.clear()
        addGroupListeners()
        addFriendListeners()
        addGuildListeners()
        addBotListeners()

        // 暂定禁止 mirai-console 的终端用户须知，它可能已不适用于 Overflow
        try {
            Class.forName("net.mamoe.mirai.console.enduserreadme.EndUserReadme")
            System.setProperty("mirai.console.skip-end-user-readme", "true")
        } catch (ignored: ClassNotFoundException) {
        }
        try {
            Class.forName("net.mamoe.mirai.console.MiraiConsole")
            miraiConsoleFlag = true
            injectMiraiConsole()
        } catch (ignored: ClassNotFoundException) {
        }
        OnebotMessages.registerSerializers()
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
    suspend fun startWithConfig(
        printInfo: Boolean = false,
        logger: Logger = LoggerFactory.getLogger("Onebot"),
        job: Job? = null
    ): Boolean {
        return start0(
            BotConfig(
                url = config.wsHost,
                reversedPort = config.reversedWSPort,
                token = config.token,
                isAccessToken = config.token.isNotBlank(),
                noPlatform = config.noPlatform,
                useCQCode = config.useCQCode,
                retryTimes = config.retryTimes,
                retryWaitMills = config.retryWaitMills,
                retryRestMills = config.retryRestMills,
                heartbeatCheckSeconds = config.heartbeatCheckSeconds,
                useGroupUploadEventForFileMessage = config.useGroupUploadEventForFileMessage,
                parentJob = job ?: defaultJob,
            ),
            printInfo = printInfo,
            logger = logger
        ) != null
    }

    // 反向 WebSocket 已存在的服务器列表
    private val reverseServerConfig = mutableMapOf<Int, OneBotProducer>()

    private suspend fun start0(
        botConfig: BotConfig,
        printInfo: Boolean = false,
        configuration: BotConfiguration? = null,
        workingDir: (Long.() -> File)? = null,
        logger: Logger = LoggerFactory.getLogger("Onebot")
    ): Bot? {
        val reversed = botConfig.isInReverseMode
        if (printInfo) {
            logger.info("Overflow v$version 正在运行")
            if (reversed) {
                logger.info("在端口 ${botConfig.reversedPort} 开启反向 WebSocket 服务端")
            } else {
                logger.info("连接到 WebSocket: ${botConfig.url}")
            }
        }

        val factory = ConnectFactory.create(botConfig, botConfig.parentJob, logger)
        val service = if (botConfig.isInReverseMode) {
            reverseServerConfig[botConfig.reversedPort]?.also {
                if (printInfo) logger.warn("在相同的端口 (${botConfig.reversedPort}) 上寻找到已创建的 ConnectFactory，已复用已有配置。")
            } ?: factory.createProducer().apply {
                invokeOnClose {
                    reverseServerConfig.remove(botConfig.reversedPort)
                }
                reverseServerConfig[botConfig.reversedPort] = this
            }
        } else {
            factory.createProducer()
        }

        service.setBotConsumer {
            logger.info("正在请求 Onebot 版本信息")
            val versionInfo = it.getVersionInfo()
            if (printInfo) {
                logger.info("协议端版本信息\n${gson.toJson(versionInfo.getAsJsonObject("data"))}")
            }
            if (it.onebotVersion == 12) {
                throw IllegalStateException("Overflow 暂不支持 Onebot 12")
            }
            val bot = it.wrap(configuration, workingDir)

            bot.eventDispatcher.broadcastAsync(BotOnlineEvent(bot))
        }
        
        val botImpl = service.awaitNewBotConnection()
        if (botImpl == null) {
            if (printInfo) logger.error("未连接到 Onebot")
            if (miraiConsole && !isNotExit) {
                OverflowCoreAsPlugin.shutdown()
                return null
            }
            return null
        }
        if (!botImpl.channel.isOpen) {
            if (printInfo) {
                logger.error("WebSocket 连接被关闭，未连接到 Onebot")
                if (miraiConsole && !isNotExit) {
                    OverflowCoreAsPlugin.shutdown()
                    return null
                }
            }
            return null
        }
        return Bot.getInstanceOrNull(botImpl.id)
    }

    override val botStarter: IBotStarter = object : IBotStarter {
        override suspend fun start(
            url: String,
            reversedPort: Int,
            token: String,
            retryTimes: Int,
            retryWaitMills: Long,
            retryRestMills: Long,
            heartbeatCheckSeconds: Int,
            printInfo: Boolean,
            noPlatform: Boolean,
            useCQCode: Boolean,
            useGroupUploadEventForFileMessage: Boolean,
            logger: Logger?,
            parentJob: Job?,
            configuration: BotConfiguration,
            workingDir: (Long.() -> File)?
        ): Bot? {
            val botConfig = BotConfig(
                url = url,
                reversedPort = reversedPort,
                token = token,
                isAccessToken = token.isNotBlank(),
                noPlatform = noPlatform,
                useCQCode = useCQCode,
                useGroupUploadEventForFileMessage = useGroupUploadEventForFileMessage,
                retryTimes = retryTimes,
                retryWaitMills = retryWaitMills,
                retryRestMills = retryRestMills,
                heartbeatCheckSeconds = heartbeatCheckSeconds,
                parentJob = parentJob
            )
            return if (logger != null) {
                start0(botConfig, printInfo, configuration, workingDir, logger)
            } else {
                start0(botConfig, printInfo, configuration, workingDir)
            }
        }
    }

    fun resolveResourceDownload(messageChain: MessageChain) {
        if (!MessageCache.enabled) return
        for (message in messageChain) {
            if (message is WrappedImage) {
                MessageCache.scheduleDownload(message)
                continue
            }
            if (message is WrappedAudio) {
                MessageCache.scheduleDownload(message)
                continue
            }
            if (message is WrappedVideo) {
                MessageCache.scheduleDownload(message)
                continue
            }
        }
        if (MessageCache.keepDuration != Duration.INFINITE) {
            launch { MessageCache.checkClean() }
        }
    }

    override fun imageFromFile(file: String): Image = OnebotMessages.imageFromFile(file)
    override fun audioFromFile(file: String): Audio = OnebotMessages.audioFromFile(file)
    override fun videoFromFile(file: String): ShortVideo = OnebotMessages.videoFromFile(file)
    override fun serializeMessage(bot: RemoteBot?, message: Message): String {
        return OnebotMessages.serializeToOneBotJson(bot, message)
    }

    override fun serializeJsonToCQCode(messageJson: String): String {
        return CQCode.fromJson(JsonParser.parseString(messageJson).asJsonArray)
    }

    override fun serializeCQCodeToJson(messageCQCode: String): String {
        return gson.toJson(CQCode.toJson(messageCQCode))
    }

    @JvmBlockingBridge
    override suspend fun deserializeMessage(bot: Bot, message: String): MessageChain {
        return OnebotMessages.deserializeFromOneBot(bot.asRemoteBot, message, null)
    }

    @JvmBlockingBridge
    override suspend fun deserializeMessageFromJson(bot: Bot, message: String): MessageChain? {
        return OnebotMessages.deserializeMessageFromJson(bot.asRemoteBot, message, null)
    }

    @JvmBlockingBridge
    override suspend fun deserializeMessageFromCQCode(bot: Bot, message: String): MessageChain? {
        return OnebotMessages.deserializeMessageFromCQCode(bot.asRemoteBot, message, null)
    }

    override fun configureMessageCache(enabled: Boolean?, saveDir: File?, keepDuration: Duration?) {
        if (enabled != null) MessageCache.enabled = enabled
        if (saveDir != null) MessageCache.saveDir = saveDir
        if (keepDuration != null) MessageCache.keepDuration = keepDuration
    }

    override suspend fun queryProfile(bot: Bot, targetId: Long): UserProfile {
        if (bot.asOnebot.appName == "shamrock") {
            val data = bot.asOnebot.impl.getUserInfo(targetId, false).data
                ?: throw IllegalStateException("Can not fetch profile card.")
            val strangerInfo = bot.asOnebot.impl.getStrangerInfo(targetId, false).data
            val sex = when (strangerInfo?.sex?.lowercase() ?: "") {
                "male" -> UserProfile.Sex.MALE
                "female" -> UserProfile.Sex.FEMALE
                else -> UserProfile.Sex.UNKNOWN
            }

            val age = strangerInfo?.age ?:
            // TODO: 不确定 birthday 的单位是毫秒还是秒
            if (data.birthday > 0) ((currentTimeSeconds() - data.birthday) / 365.daysToSeconds).toInt() else 0

            return UserProfileImpl(age, data.mail, 0, data.name, data.level, sex, data.hobbyEntry)
        } else {
            val data = bot.asOnebot.impl.getStrangerInfo(targetId, false).data
                ?: throw IllegalStateException("Can not fetch stranger info (profile card).")
            val sex = when (data.sex.lowercase()) {
                "male" -> UserProfile.Sex.MALE
                "female" -> UserProfile.Sex.FEMALE
                else -> UserProfile.Sex.UNKNOWN
            }
            return UserProfileImpl(data.age, "", 0, data.nickname, data.level, sex, "")
        }
    }

    override suspend fun getOnlineOtherClientsList(bot: Bot, mayIncludeSelf: Boolean): List<OtherClientInfo> {
        val data = bot.asOnebot.impl.getOnlineClients(false).data
            ?: throw IllegalStateException("Can not fetch online clients.")
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
        return bot.asOnebot.impl.getForwardMsg(resourceId).data?.message?.map {
            val msg = OnebotMessages.deserializeFromOneBot(bot.asRemoteBot, it.message)
            resolveResourceDownload(msg)
            ForwardMessage.Node(
                it.sender!!.userId,
                it.time,
                it.sender!!.nickname.takeIf(String::isNotEmpty) ?: "QQ用户",
                msg
            )
        } ?: throw IllegalStateException("无法下载转发消息，详见网络日志 (logs/onebot/*.log")
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
        val impl = FriendInfoResp().apply {
            userId = friendInfo.uin
            nickname = friendInfo.nick
            remark = friendInfo.remark
        }
        return FriendWrapper(bot.asOnebot, impl, gson.toJsonTree(impl))
    }

    @LowLevelApi
    override fun newStranger(bot: Bot, strangerInfo: StrangerInfo): Stranger {
        val impl = strangerInfo.asOnebot
        return StrangerWrapper(bot.asOnebot, impl, gson.toJsonTree(impl))
    }

    override suspend fun queryImageUrl(bot: Bot, image: Image): String {
        val extUrl = MediaURLService.instances.queryImageUrl(bot.asRemoteBot, image)
        if (extUrl != null) return extUrl
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
        val onebot = bot.asOnebot
        when (onebot.appName.lowercase()) {
            "llonebot", "napcat" -> {
                if (receiver is Group) {
                    onebot.impl.extGroupPoke(receiver.id, nudge.target.id)
                } else {
                    onebot.impl.extFriendPoke(receiver.id)
                }
                return true
            }
        }
        // go-cqhttp
        val msg = "[{\"type\":\"poke\",\"data\":{\"id\":${nudge.target.id}}}]"
        if (receiver is Group) {
            onebot.impl.sendGroupMsg(receiver.id, msg, false)
        } else {
            onebot.impl.sendPrivateMsg(receiver.id, msg, false)
        }
        return true
    }


    //========== Bot Invited Join Group Request 邀请机器人加群请求 START =============
    fun putInventedJoinGroupRequestFlag(flag: String): Long {
        var eventId = newInviteJoinGroupRequestFlagMap.size.toLong()
        while (newInviteJoinGroupRequestFlagMap.containsKey(eventId)) {
            eventId++
        }
        return eventId.also { newInviteJoinGroupRequestFlagMap[it] = flag }
    }

    override suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) {
        solveBotInvitedJoinGroupRequestEvent(event.bot, event.eventId, event.invitorId, event.groupId, true)
    }

    override suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent) {
        solveBotInvitedJoinGroupRequestEvent(event.bot, event.eventId, event.invitorId, event.groupId, false)
    }

    @LowLevelApi
    override suspend fun solveBotInvitedJoinGroupRequestEvent(
        bot: Bot,
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    ) {
        newInviteJoinGroupRequestFlagMap[eventId]?.also {
            bot.asOnebot.impl.setGroupAddRequest(it, "invite", accept, "")
        }
    }
    //========== Bot Invited Join Group Request 邀请机器人加群请求 END =============


    //========== Member Join Request 加群申请 START =============
    fun putMemberJoinRequestFlag(flag: String): Long {
        var eventId = newMemberJoinRequestFlagMap.size.toLong()
        while (newMemberJoinRequestFlagMap.containsKey(eventId)) {
            eventId++
        }
        return eventId.also { newMemberJoinRequestFlagMap[it] = flag }
    }

    override suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent) {
        solveMemberJoinRequestEvent(
            event.bot,
            event.eventId,
            event.fromId,
            event.fromNick,
            event.groupId,
            accept = true,
            blackList = false,
            message = event.message
        )
    }

    override suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean, message: String) {
        solveMemberJoinRequestEvent(
            event.bot,
            event.eventId,
            event.fromId,
            event.fromNick,
            event.groupId,
            accept = false,
            blackList = blackList,
            message = message
        )
    }

    override suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean) {
        solveMemberJoinRequestEvent(
            event.bot,
            event.eventId,
            event.fromId,
            event.fromNick,
            event.groupId,
            accept = null,
            blackList = blackList,
            message = event.message
        )
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
    fun putNewFriendRequestFlag(flag: String): Long {
        var eventId = newFriendRequestFlagMap.size.toLong()
        while (newFriendRequestFlagMap.containsKey(eventId)) {
            eventId++
        }
        return eventId.also { newFriendRequestFlagMap[it] = flag }
    }

    override suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent) {
        solveNewFriendRequestEvent(
            event.bot,
            event.eventId,
            event.fromId,
            event.fromNick,
            accept = true,
            blackList = false
        )
    }

    override suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean) {
        solveNewFriendRequestEvent(event.bot, event.eventId, event.fromId, event.fromNick, false, blackList)
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
        newFriendRequestFlagMap[eventId]?.also {
            bot.asOnebot.impl.setFriendAddRequest(it, accept, "")
        }
    }
    //========== New Friend Request 新好友请求 END =============

}

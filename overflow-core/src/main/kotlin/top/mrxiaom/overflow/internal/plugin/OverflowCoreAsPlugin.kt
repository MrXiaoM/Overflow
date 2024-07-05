package top.mrxiaom.overflow.internal.plugin

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.events.StartupEvent
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.console.util.sendAnsiMessage
import net.mamoe.mirai.contact.remarkOrNick
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.weeksToMillis
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.CloseFrame
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import top.mrxiaom.overflow.BotBuilder
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.contact.RemoteBot.Companion.asRemoteBot
import top.mrxiaom.overflow.event.UnsolvedOnebotEvent
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.asOnebot
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.utils.LoggerInFolder
import java.io.File
import kotlin.reflect.jvm.jvmName

@Suppress("PluginMainServiceNotConfigured", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object OverflowCoreAsPlugin : Plugin, CommandOwner {
    override val isEnabled: Boolean get() = true

    override val loader: PluginLoader<*, *> get() = TheLoader

    override val parentPermission: Permission
        get() = ConsoleCommandOwner.parentPermission

    private val logger: MiraiLogger
        get() = OverflowAPI.logger
    private lateinit var miraiLogger: MiraiLogger
    private lateinit var oneBotLogger: Logger
    internal lateinit var channel: EventChannel<Event>
    internal var autoConnect = true

    override fun permissionId(name: String): PermissionId {
        return ConsoleCommandOwner.permissionId(name)
    }

    @OptIn(ConsoleExperimentalApi::class, ConsoleFrontEndImplementation::class)
    fun onEnable() {
        if (Overflow.instance.config.noLogDoNotReportIfYouSwitchThisOn) {
            miraiLogger = MiraiLogger.Factory.create(Overflow::class, "Onebot")
            miraiLogger.warning("你已开启 no_log，开启该选项后将不接受漏洞反馈。")
        } else {
            miraiLogger = LoggerInFolder(Overflow::class, "Onebot", File("logs/onebot"), 1.weeksToMillis)
        }
        oneBotLogger = net.mamoe.mirai.console.internal.logging.externalbind.slf4j.SLF4JAdapterLogger(miraiLogger)

        channel = GlobalEventChannel
            .parentScope(MiraiConsole.INSTANCE)
            .context(MiraiConsole.INSTANCE.coroutineContext)

        channel.subscribeOnce<StartupEvent>(
                priority = EventPriority.HIGHEST,
            ) { onPostStartup() }

        channel.subscribeAlways<UnsolvedOnebotEvent>(priority = EventPriority.LOWEST) {
                if (!isCancelled) {
                    logger.warning("接收到来自协议端的未知事件 $messageRaw")
                }
            }

        OnebotMessages.registerSerializers()

        // No AutoLogin
        val dataScope: MiraiConsoleImplementation.ConsoleDataScope = net.mamoe.mirai.console.internal.data.builtins.DataScope
        dataScope.find(AutoLoginConfig::class)?.run {
            if (accounts.isNotEmpty()) {
                val configFolder = MiraiConsoleImplementation.getInstance().rootPath.resolve("config").toFile()

                val file = File(configFolder, "Console/AutoLogin.yml")
                val backup = File(configFolder, "Console/AutoLogin.yml.overflow.${System.currentTimeMillis()}.old")
                file.copyTo(backup)
                accounts.clear()
                logger.warning("由于 mirai 端不再需要处理登录，Overflow 已清空自动登录配置，旧配置已备份到 ${backup.name}")
            }
        }

        // Unregister unnecessary commands
        val unregisterCommands = arrayOf("login", "autoLogin", "status")
        CommandManager.INSTANCE.allRegisteredCommands.filter {
            it.owner == ConsoleCommandOwner && unregisterCommands.contains(it.primaryName)
        }.forEach(CommandManager.INSTANCE::unregisterCommand)

        BuiltInCommands.StatusCommand.register()

        object : CompositeCommand(
            owner = this,
            primaryName = "overflow",
            secondaryNames = arrayOf(),
        ) {

            @SubCommand
            @Description("查看在线 Bot 列表")
            suspend fun CommandSender.bots() {
                sendMessage(Bot.instances.joinToString { it.id.toString() })
            }
            @SubCommand
            @Description("查看 Bot")
            suspend fun CommandSender.bot(id: Long) {
                val bot = Bot.findInstance(id)
                if (bot == null) {
                    sendMessage("bot 不存在")
                    return
                }
                sendMessage("id=${bot.id}, nick=${bot.nick}")
            }
            @SubCommand
            @Description("重新连接 Onebot")
            suspend fun CommandSender.reconnect(
                @Name("QQ号") qq: Long? = null
            ) {
                if (Bot.instances.isEmpty()) startWithConfig()
                else Bot.instances.filter { qq == null || it.id == qq }.forEach {
                    when (val channel = it.asOnebot.impl.channel) {
                        is WebSocketClient -> channel.reconnectBlocking()
                        is WebSocketServer -> channel.close(CloseFrame.NORMAL, "主动关闭 (重连)")
                    }
                }
                sendMessage("重新连接执行完成")
            }

            @SubCommand
            @Description("通过 WebSocket 连接到多个 Onebot 实现 (实验性)")
            suspend fun CommandSender.connect(
                @Name("WS类型") wsType: WebSocketType,
                @Name("正向地址或反向端口") hostOrPort: String,
                @Name("是否为QQ平台") platform: Boolean = true,
                @Name("连接令牌") token: String? = null
            ) {
                val finalBot = when (wsType) {
                    WebSocketType.POSITIVE -> BotBuilder.positive(hostOrPort)
                    WebSocketType.REVERSED -> BotBuilder.reversed(hostOrPort.toIntOrNull()?.takeIf { it in 0..65535 } ?: return)
                }.also {
                    if (token != null) it.token(token)
                    if (!platform) it.noPlatform()
                    val config = Overflow.instance.config
                    it.retryTimes(config.retryTimes)
                    it.retryWaitMills(config.retryWaitMills)
                    it.retryRestMills(config.retryRestMills)
                    it.overrideLogger(oneBotLogger)
                }.connect()

                if (finalBot != null) {
                    sendMessage("${finalBot.id} 登录成功，昵称为 ${finalBot.nick}")
                } else {
                    sendMessage("登录失败，详见控制台日志")
                }
            }

            @SubCommand
            @Description("调用API")
            suspend fun CommandSender.exec(
                @Name("API请求路径") apiPath: String,
                @Name("请求参数") params : String?,
                @Name("发送回应") showRet : Boolean = true 
            ) {
                val bot = (bot ?: Bot.instances.firstOrNull())?.asRemoteBot ?: return Unit.also {
                    sendMessage("至少有一个Bot在线才能执行该命令")
                }
                val ret = bot.executeAction(apiPath,params)
                if (showRet) {
                    sendMessage(ret)
                }
            }

            @SubCommand
            @Description("获取群聊列表")
            suspend fun CommandSender.groups(
                @Name("详细") details: Boolean = false
            ) {
                val bot = (bot ?: Bot.instances.firstOrNull()) ?: return Unit.also {
                    sendMessage("至少有一个Bot在线才能执行该命令")
                }
                if (!details) {
                    sendMessage("${bot.id} 的群数量: ${bot.groups.size}")
                } else {
                    sendMessage("共  ${bot.groups.size} 个群: " +
                        bot.groups.joinToString(", ") { "[${it.id}:${it.name}]" }
                    )
                }
            }

            @SubCommand
            @Description("发送群消息")
            suspend fun CommandSender.group(
                @Name("群号") groupId: Long,
                @Name("消息") vararg message: String
            ) {
                val bot = (bot ?: Bot.instances.firstOrNull()) ?: return Unit.also {
                    sendMessage("至少有一个Bot在线才能执行该命令")
                }
                val group = bot.groups[groupId] ?: return Unit.also {
                    sendMessage("找不到群 $groupId")
                }
                // TODO: 用更简洁的方法反序列化消息
                val messageChain = OnebotMessages.deserializeFromOneBot(bot.asRemoteBot, message.joinToString(" "))
                group.sendMessage(messageChain)
                sendMessage("消息发送成功")
            }

            @SubCommand
            @Description("获取群聊列表")
            suspend fun CommandSender.friends(
                @Name("详细") details: Boolean = false
            ) {
                val bot = (bot ?: Bot.instances.firstOrNull()) ?: return Unit.also {
                    sendMessage("至少有一个Bot在线才能执行该命令")
                }
                if (!details) {
                    sendMessage("${bot.id} 的好友数量: ${bot.friends.size}")
                } else {
                    sendMessage("${bot.id} 共有 ${bot.friends.size} 个好友: " +
                        bot.friends.joinToString(", ") { "[${it.id}:${it.remarkOrNick}]" }
                    )
                }
            }

            @SubCommand
            @Description("发送好友消息")
            suspend fun CommandSender.friend(
                @Name("好友QQ") friendId: Long,
                @Name("消息") vararg message: String
            ) {
                val bot = (bot ?: Bot.instances.firstOrNull()) ?: return Unit.also {
                    sendMessage("至少有一个Bot在线才能执行该命令")
                }
                val friend = bot.friends[friendId] ?: return Unit.also {
                    sendMessage("找不到好友 $friendId")
                }
                // TODO: 用更简洁的方法反序列化消息
                val messageChain = OnebotMessages.deserializeFromOneBot(bot.asRemoteBot, message.joinToString(" "))
                friend.sendMessage(messageChain)
                sendMessage("消息发送成功")
            }
        }.register()

    }

    @Suppress("DEPRECATION_ERROR")
    private suspend fun StartupEvent.onPostStartup() {
        runCatching {
            net.mamoe.mirai.internal.spi.EncryptService.factory?.also {
                logger.apply {
                    warning("-------------------------------------------")
                    warning("你的 mirai-console 中已安装签名服务!")
                    warning("这在 overflow 中是不必要的，请移除签名服务相关插件")
                    warning("当前已装载签名服务: ${it::class.jvmName}")
                    warning("位置: ${it::class.java.classLoader}")
                    warning("-------------------------------------------")
                }
            }
        }
        ConsoleCommandSender.sendAnsiMessage {
            appendLine()
            reset()
            append("如需关闭控制台，请按下 ")
            lightBlue().append("Ctrl+C").reset().append(" 组合键，或使用命令 ").lightBlue().append("/stop").reset().append(" ")
            append("来关闭。强制")
            lightYellow().append("结束进程/关闭窗口").reset()
            append("可能会导致")
            lightRed().append("数据丢失").reset()
            appendLine()
            appendLine()
        }
        if (autoConnect) startWithConfig()
    }

    internal suspend fun startWithConfig(printInfo: Boolean = true) {
        Overflow.instance.startWithConfig(printInfo, oneBotLogger)
    }

    enum class WebSocketType {
        POSITIVE, REVERSED
    }

    internal object TheLoader : PluginLoader<Plugin, PluginDescription> {
        override fun listPlugins(): List<Plugin> = listOf(OverflowCoreAsPlugin)

        override fun disable(plugin: Plugin) {
        }

        override fun enable(plugin: Plugin) {
            if (plugin !== OverflowCoreAsPlugin) return
            plugin.onEnable()
        }

        override fun load(plugin: Plugin) {
            // this would never run
        }

        override fun getPluginDescription(plugin: Plugin): PluginDescription {
            if (plugin !== OverflowCoreAsPlugin) {
                error("loader not match with " + plugin.description.id)
            }
            return TheDescription
        }
    }

    internal object TheDescription : PluginDescription {
        override val id: String get() = "top.mrxiaom.overflow"
        override val name: String get() = "溢出核心"
        override val author: String get() = "MrXiaoM"
        override val version: SemVersion get() = SemVersion(Overflow.version)
        override val info: String get() = ""
        override val dependencies: Set<PluginDependency> get() = setOf()

        override fun toString(): String {
            return "PluginDescription[ overflow-core ]"
        }
    }
}

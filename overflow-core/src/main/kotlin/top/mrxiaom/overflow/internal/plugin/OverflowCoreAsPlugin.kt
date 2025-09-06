package top.mrxiaom.overflow.internal.plugin

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.events.StartupEvent
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.console.util.sendAnsiMessage
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.weeksToMillis
import org.slf4j.Logger
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.event.UnsolvedOnebotEvent
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.plugin.OverflowCoreAsPlugin.TheDescription.id
import top.mrxiaom.overflow.internal.utils.LoggerInFolder
import java.io.File
import kotlin.reflect.jvm.jvmName
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Suppress("PluginMainServiceNotConfigured", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object OverflowCoreAsPlugin : Plugin, CommandOwner {
    override val isEnabled: Boolean get() = true

    override val loader: PluginLoader<*, *> get() = TheLoader

    override val parentPermission: Permission
        get() = ConsoleCommandOwner.parentPermission

    private val logger: MiraiLogger
        get() = OverflowAPI.logger
    internal lateinit var miraiLogger: MiraiLogger
    internal lateinit var oneBotLogger: Logger
    internal lateinit var channel: EventChannel<Event>
    internal var autoConnect = true
    private val closingLock = Mutex()
    override fun permissionId(name: String): PermissionId {
        return ConsoleCommandOwner.permissionId(name)
    }

    @OptIn(DelicateCoroutinesApi::class, ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class)
    fun shutdown() {
        GlobalScope.launch {
            kotlin.runCatching {
                closingLock.withLock {
                    if (!MiraiConsole.isActive) return@withLock
                    ConsoleCommandSender.sendMessage("Stopping mirai-console")
                    kotlin.runCatching {
                        MiraiConsoleImplementation.shutdown()
                    }.fold(
                        onSuccess = {
                            try {
                                ConsoleCommandSender.sendMessage("mirai-console stopped successfully.")
                            } catch (ignored: EventCancelledException) {}
                        },
                        onFailure = {
                            MiraiConsole.mainLogger.error("Exception in stop", it)
                            try {
                                ConsoleCommandSender.sendMessage(
                                    it.localizedMessage ?: it.message ?: it.toString()
                                )
                            } catch (ignored: EventCancelledException) {}
                        }
                    )
                }
            }.exceptionOrNull()?.let(MiraiConsole.mainLogger::error)
            exitProcess(0)
        }
    }

    @OptIn(ConsoleExperimentalApi::class, ConsoleFrontEndImplementation::class)
    fun onEnable() {
        val overflow = Overflow.instance
        val config = overflow.config
        if (config.noLogDoNotReportIfYouSwitchThisOn) {
            miraiLogger = MiraiLogger.Factory.create(Overflow::class, "Onebot")
            miraiLogger.warning("你已开启 no_log，开启该选项后将不接受漏洞反馈。")
        } else {
            miraiLogger = LoggerInFolder(Overflow::class, "Onebot", File("logs/onebot"), 1.weeksToMillis)
        }
        oneBotLogger = net.mamoe.mirai.console.internal.logging.externalbind.slf4j.SLF4JAdapterLogger(miraiLogger)

        config.resourceCache.apply {
            overflow.configureMessageCache(
                enabled = enabled,
                saveDir = PluginManager.pluginsDataFolder
                    .resolve("$id/cache")
                    .also { if (enabled && !it.exists()) it.mkdirs() },
                keepDuration = keepDurationHours
                    .takeIf { it > 0 }?.hours
                    ?: Duration.INFINITE
            )
        }

        channel = GlobalEventChannel
            .parentScope(MiraiConsole.INSTANCE)
            .context(MiraiConsole.INSTANCE.coroutineContext)

        channel.subscribeOnce<StartupEvent>(
                priority = EventPriority.HIGHEST,
            ) { onPostStartup() }

        channel.subscribeAlways<UnsolvedOnebotEvent>(priority = EventPriority.LOWEST) {
                if (!isCancelled) {
                    if (json["post_type"]?.string?.equals("meta_event") == false) {
                        logger.warning("接收到来自协议端的未知事件 $messageRaw")
                    }
                }
            }

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
        BuiltInCommands.OverflowCommand.register()

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
        if (Overflow.instance.config.token.length <= 8) {
            ConsoleCommandSender.sendAnsiMessage {
                appendLine()
                reset().lightRed()
                append("  (!) ")
                reset().lightYellow()
                append("请注意，你没有设置 token 或 token 长度过短，如果你的 Onebot 服务暴露在公网中，将有可能被有心人士利用进行非法操作。").reset()
                appendLine()
            }
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

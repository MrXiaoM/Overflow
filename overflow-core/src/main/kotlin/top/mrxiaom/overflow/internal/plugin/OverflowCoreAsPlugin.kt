package top.mrxiaom.overflow.internal.plugin

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.extensions.PostStartupExtension
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
import net.mamoe.mirai.utils.weeksToMillis
import org.java_websocket.client.WebSocketClient
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

    override fun permissionId(name: String): PermissionId {
        return ConsoleCommandOwner.permissionId(name)
    }

    fun net.mamoe.mirai.console.internal.extension.AbstractConcurrentComponentStorage.contributePostStartupExtension(
        instance: PostStartupExtension
    ): Unit = contribute(PostStartupExtension, this@OverflowCoreAsPlugin, lazyInstance = { instance })

    @OptIn(ConsoleExperimentalApi::class, ConsoleFrontEndImplementation::class)
    suspend fun onEnable() {
        val miraiLogger = LoggerInFolder(Overflow::class, "Onebot", File("logs/onebot"), 1.weeksToMillis)
        val oneBotLogger = net.mamoe.mirai.console.internal.logging.externalbind.slf4j.SLF4JAdapterLogger(miraiLogger)

        MiraiConsoleImplementation.getBridge().globalComponentStorage.contributePostStartupExtension {
            onPostStartup()
        }

        OnebotMessages.registerSerializers()
        Overflow.instance.start(true, oneBotLogger)

        // keep a command register example here

        object : CompositeCommand(
            owner = this,
            primaryName = "overflow",
            secondaryNames = arrayOf(),
        ) {
            @SubCommand
            @Description("重新连接 Onebot")
            suspend fun CommandSender.reconnect() {
                if (Bot.instances.isEmpty()) Overflow.instance.start(true, oneBotLogger)
                else Bot.instances.forEach {
                    // TODO: WebSocketServer
                    (it.asOnebot.impl.channel as? WebSocketClient)?.reconnectBlocking()
                }
                sendMessage("重新连接执行完成")
            }
            @SubCommand
            @Description("发送群消息")
            suspend fun CommandSender.group(
                @Name("群号") groupId: Long,
                @Name("消息")vararg message: String
            ) {
                val bot = Bot.instances.firstOrNull() ?: return Unit.also {
                    sendMessage("至少有一个Bot在线才能执行该命令")
                }
                val group = bot.groups[groupId] ?: return Unit.also {
                    sendMessage("找不到群 $groupId")
                }
                // TODO: 用更简洁的方法反序列化消息
                val messageChain = OnebotMessages.deserializeFromOneBot(bot, message.joinToString(" "))
                group.sendMessage(messageChain)
                sendMessage("消息发送成功")
            }
            @SubCommand
            @Description("发送好友消息")
            suspend fun CommandSender.friend(
                @Name("好友QQ") friendId: Long,
                @Name("消息") vararg message: String
            ) {
                val bot = Bot.instances.firstOrNull() ?: return Unit.also {
                    sendMessage("至少有一个Bot在线才能执行该命令")
                }
                val friend = bot.friends[friendId] ?: return Unit.also {
                    sendMessage("找不到好友 $friendId")
                }
                // TODO: 用更简洁的方法反序列化消息
                val messageChain = OnebotMessages.deserializeFromOneBot(bot, message.joinToString(" "))
                friend.sendMessage(messageChain)
                sendMessage("消息发送成功")
            }
        }.register()

        // No AutoLogin
        val dataScope: MiraiConsoleImplementation.ConsoleDataScope = net.mamoe.mirai.console.internal.data.builtins.DataScope
        dataScope.find(AutoLoginConfig::class)?.run {
            if (accounts.isNotEmpty()) {
                val configFolder = MiraiConsoleImplementation.getInstance().rootPath.resolve("config").toFile()

                val file = File(configFolder, "Console/AutoLogin.yml")
                val backup = File(configFolder, "Console/AutoLogin.yml.overflow.${System.currentTimeMillis()}.old")
                file.copyTo(backup)
                accounts.clear()
                Overflow.logger.warning("由于 mirai 端不再需要处理登录，Overflow 已清空自动登录配置，旧配置已备份到 ${backup.name}")
            }
        }
        val unregisterCommands = arrayOf("login", "autoLogin")
        CommandManager.INSTANCE.allRegisteredCommands.filter {
            it.owner == ConsoleCommandOwner && unregisterCommands.contains(it.primaryName)
        }.forEach {
            CommandManager.INSTANCE.unregisterCommand(it)
        }
    }

    @Suppress("DEPRECATION_ERROR")
    fun onPostStartup() {
        net.mamoe.mirai.internal.spi.EncryptService.factory?.also {
            Overflow.logger.apply {
                warning("-------------------------------------------")
                warning("你的 mirai-console 中已安装签名服务!")
                warning("这在 overflow 中是不必要的，请移除签名服务相关插件")
                warning("当前已装载签名服务: ${it::class.jvmName}")
                warning("位置: ${it::class.java.classLoader}")
                warning("-------------------------------------------")
            }
        }
    }

    internal object TheLoader : PluginLoader<Plugin, PluginDescription> {
        override fun listPlugins(): List<Plugin> = listOf(OverflowCoreAsPlugin)

        override fun disable(plugin: Plugin) {
        }

        override fun enable(plugin: Plugin) {
            if (plugin !== OverflowCoreAsPlugin) return
            runBlocking(CoroutineName("OverflowPluginLoader")) {
                plugin.onEnable()
            }
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
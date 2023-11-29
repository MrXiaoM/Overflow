package top.mrxiaom.overflow.internal.plugin

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
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
import top.mrxiaom.overflow.internal.BuildConstants
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.message.OnebotMessages
import top.mrxiaom.overflow.utils.LoggerInFolder
import java.io.File

@Suppress("PluginMainServiceNotConfigured")
internal object OverflowCoreAsPlugin : Plugin, CommandOwner {
    override val isEnabled: Boolean get() = true

    override val loader: PluginLoader<*, *> get() = TheLoader

    override val parentPermission: Permission
        get() = ConsoleCommandOwner.parentPermission

    override fun permissionId(name: String): PermissionId {
        return ConsoleCommandOwner.permissionId(name)
    }

    @OptIn(ConsoleExperimentalApi::class)
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    suspend fun onEnable() {
        val miraiLogger = LoggerInFolder(Overflow::class, "Onebot", File("logs/onebot"), 1.weeksToMillis)
        val logger = net.mamoe.mirai.console.internal.logging.externalbind.slf4j.SLF4JAdapterLogger(miraiLogger)

        OnebotMessages.registerSerializers()
        Overflow.instance.start(true, logger)

        // keep a command register example here

        object : CompositeCommand(
            owner = this,
            primaryName = "overflow",
            secondaryNames = arrayOf(),
        ) {
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
                val messageChain = OnebotMessages.deserializeFromOneBotJson(bot, message.joinToString(" "))
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
                val messageChain = OnebotMessages.deserializeFromOneBotJson(bot, message.joinToString(" "))
                friend.sendMessage(messageChain)
                sendMessage("消息发送成功")
            }
        }.register()
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
        override val version: SemVersion get() = SemVersion(top.mrxiaom.overflow.internal.BuildConstants.VERSION)
        override val info: String get() = ""
        override val dependencies: Set<PluginDependency> get() = setOf()


        override fun toString(): String {
            return "PluginDescription[ overflow-core ]"
        }
    }
}
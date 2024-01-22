@file:OptIn(ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class, MiraiExperimentalApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package top.mrxiaom.overflow.internal.plugin

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.permission.BuiltInPermissionService
import net.mamoe.mirai.console.internal.plugin.JvmPluginInternal
import net.mamoe.mirai.console.internal.plugin.MiraiConsoleAsPlugin
import net.mamoe.mirai.console.internal.pluginManagerImpl
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import top.mrxiaom.overflow.BuildConstants
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.message.OnebotMessages
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.MemoryUsage
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

internal object BuiltInCommands {
    object StatusCommand : SimpleCommand(
        ConsoleCommandOwner, "status", "states", "状态",
        description = "获取 Mirai Console 运行状态"
    ), Command, BuiltInCommand {

        internal interface MemoryUsageGet {
            val heapMemoryUsage: MUsage
            val nonHeapMemoryUsage: MUsage
            val objectPendingFinalizationCount: Int
        }

        private val memoryUsageGet: MemoryUsageGet = kotlin.runCatching {
            ByMemoryMXBean
        }.getOrElse { ByRuntime }

        internal object ByMemoryMXBean : MemoryUsageGet {
            private val memoryMXBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()
            private val MemoryUsage.m: MUsage
                get() = MUsage(
                    committed, init, used, max
                )
            override val heapMemoryUsage: MUsage
                get() = memoryMXBean.heapMemoryUsage.m
            override val nonHeapMemoryUsage: MUsage
                get() = memoryMXBean.nonHeapMemoryUsage.m
            override val objectPendingFinalizationCount: Int
                get() = memoryMXBean.objectPendingFinalizationCount
        }

        internal object ByRuntime : MemoryUsageGet {
            override val heapMemoryUsage: MUsage
                get() {
                    val runtime = Runtime.getRuntime()
                    return MUsage(
                        committed = 0,
                        init = 0,
                        used = runtime.maxMemory() - runtime.freeMemory(),
                        max = runtime.maxMemory()
                    )
                }
            override val nonHeapMemoryUsage: MUsage
                get() = MUsage(-1, -1, -1, -1)
            override val objectPendingFinalizationCount: Int
                get() = -1
        }

        internal data class MUsage(
            val committed: Long,
            val init: Long,
            val used: Long,
            val max: Long,
        )

        private fun dateTime(timestamp: Long): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(timestamp))
        }

        private fun time(time: Long): String = buildString {
            val seconds = time / 1000
            val minutes = (seconds / 60) % 60
            val hours = (seconds / 60 / 60) % 24
            val days = seconds / 60 / 60 / 24
            if (days > 0) append(days).append("天 ")
            if (hours > 0 || days > 0) append(hours).append("时 ")
            append(minutes).append("分")
        }

        @Handler
        suspend fun CommandSender.handle() {
            sendAnsiMessage {
                val buildDateFormatted =
                    MiraiConsoleBuildConstants.buildDate.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                append("正在运行 MiraiConsole v")
                gold().append(MiraiConsoleBuildConstants.versionConst)
                reset().append(", 构建于 ")
                lightBlue().append(buildDateFormatted).reset().append(".\n")
                append("正在运行 Overflow v")
                gold().append(BuildConstants.VERSION)
                reset().append(".\n")
                append(MiraiConsoleImplementation.getInstance().frontEndDescription.render()).append("\n")
                append("启动时间: ").green().append(dateTime(Overflow.instance.startupTime)).reset().append(", 已运行 ").green().append(time(System.currentTimeMillis() - Overflow.instance.startupTime)).reset().append(".\n\n")

                append("权限服务: ").append(
                    if (PermissionService.INSTANCE is BuiltInPermissionService) {
                        lightYellow()
                        "内置权限服务"
                    } else {
                        val plugin = GlobalComponentStorage.getPreferredExtension(PermissionServiceProvider).plugin
                        if (plugin == null) {
                            PermissionService.INSTANCE.toString()
                        } else {
                            green().append(plugin.name).reset().append(" v").gold()
                            plugin.version.toString()
                        }
                    }
                )
                reset().append("\n")
                append("Onebot 实现信息: ")
                green().append(OnebotMessages.appName).reset().append(" v")
                gold().append(OnebotMessages.appVersion).reset().append(".\n\n")

                append("插件列表 (${MiraiConsole.pluginManagerImpl.resolvedPlugins.size - 2}): ")

                val resolvedPlugins = MiraiConsole.pluginManagerImpl.resolvedPlugins.asSequence()
                    .filter { it !is MiraiConsoleAsPlugin } // skip mirai-console in status
                    .filter { it !is OverflowCoreAsPlugin } // skip overflow in status
                    .toList()

                if (resolvedPlugins.isEmpty()) {
                    gray().append("<无>")
                } else {
                    resolvedPlugins.joinTo(this) { plugin ->
                        if (plugin.isEnabled) {
                            green().append(plugin.name).reset().append(" v").gold()
                        } else {
                            red().append(plugin.name)
                            if (plugin is JvmPluginInternal) {
                                append("(").append(plugin.currentPluginStatus.name.lowercase()).append(")")
                            } else {
                                append("(已禁用)")
                            }
                            reset().append(" v").gold()
                        }
                        plugin.version.toString()
                    }
                }
                reset().append("\n\n")

                append("已挂起待回收对象数: ")
                    .emeraldGreen()
                    .append(memoryUsageGet.objectPendingFinalizationCount)
                    .reset()
                    .append("\n")
                val l1 = arrayOf("已提交", "初始", "已使用", "最大")
                val l2 = renderMemoryUsage(memoryUsageGet.heapMemoryUsage)
                val l3 = renderMemoryUsage(memoryUsageGet.nonHeapMemoryUsage)
                val lmax = calculateMax(l1, l2.first, l3.first)

                append("  　　　　　　")
                l1.forEachIndexed { index, s ->
                    if (index != 0) append(" | ")
                    val s1 = lmax[index] - s.length * 1.5
                    val left = s1 / 2
                    val right = s1 - left
                    emptyLine(left.toInt())
                    append(s); reset()
                    emptyLine(right.toInt())
                }
                reset()
                append("\n")

                fun rendMU(l: Pair<Array<String>, LongArray>) {
                    val max = l.second[3]
                    val e50 = max / 2
                    val e90 = max * 90 / 100
                    l.first.forEachIndexed { index, s ->
                        if (index != 0) append(" | ")
                        renderMUNum(lmax[index], s.length) {
                            if (index == 3) {
                                // MAX
                                append(s)
                            } else {
                                if (max < 0L) {
                                    append(s)
                                } else {
                                    val v = l.second[index]
                                    when {
                                        v < e50 -> {
                                            green()
                                        }

                                        v < e90 -> {
                                            lightRed()
                                        }

                                        else -> {
                                            red()
                                        }
                                    }
                                    append(s)
                                    reset()
                                }
                            }
                        }
                    }
                }

                append("   　堆内存: ")
                rendMU(l2)
                append("\n   非堆内存: ")
                rendMU(l3)
            }
        }

        private fun renderMemoryUsage(usage: MUsage) = arrayOf(
            renderMemoryUsageNumber(usage.committed),
            renderMemoryUsageNumber(usage.init),
            renderMemoryUsageNumber(usage.used),
            renderMemoryUsageNumber(usage.max),
        ) to longArrayOf(
            usage.committed,
            usage.init,
            usage.used,
            usage.max,
        )

        private fun calculateMax(
            vararg lines: Array<String>
        ): IntArray = IntArray(lines[0].size) { r ->
            lines.maxOf { it[r].length }
        }
    }
}
package top.mrxiaom.overflow

import kotlinx.coroutines.Job
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import org.slf4j.Logger
import java.io.File
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Onebot 连接向导
 */
public class BotBuilder private constructor(
    private var url: String = "ws://127.0.0.1:8080",
    private var reversedPort: Int = -1,
    private var token: String = "",
    private var retryTimes: Int = 5,
    private var retryWaitMills: Long = 5000L,
    private var retryRestMills: Long = 60000L,
    private var printInfo: Boolean = true,
    private var noPlatform: Boolean = false,
    private var useCQCode: Boolean = false,
    private var logger: Logger? = null,
    private var parentJob: Job? = null,
    private var configuration: BotConfiguration = BotConfiguration {
        botLoggerSupplier = { MiraiLogger.Factory.create(Bot::class, "Bot.${it.id}") }
        networkLoggerSupplier = { MiraiLogger.Factory.create(Bot::class, "Net.${it.id}") }
    },
    private var workingDir: (Long.() -> File)? = null,
) {
    /**
     * 设置用于连接时鉴权的 token
     *
     * 将 token 设为空则不进行鉴权
     */
    public fun token(token: String): BotBuilder = apply {
        this.token = token
    }

    /**
     * 设置尝试重连次数，设为 0 时禁用自动重连
     *
     * 仅主动 WebSocket 可自动重连
     */
    public fun retryTimes(retryTimes: Int): BotBuilder = apply {
        this.retryTimes = retryTimes
    }

    /**
     * 设置重连等待间隔时间
     *
     * 仅主动 WebSocket 可自动重连
     */
    public fun retryWaitMills(retryWaitMills: Long): BotBuilder = apply {
        this.retryWaitMills = retryWaitMills
    }

    /**
     * 设置重连休息时间
     *
     * 仅主动 WebSocket 可自动重连
     *
     * 重连次数耗尽后，会进入休息状态，休息结束后重置重连次数，再次尝试重连
     *
     * 设为 -1 禁用此功能
     */
    public fun retryRestMills(retryRestMills: Long): BotBuilder = apply {
        this.retryRestMills = retryRestMills
    }

    /**
     * 禁止打印连接时额外状态信息
     *
     * 如 Overflow 版本、连接地址、Onebot 协议端版本等
     *
     * 如果需要禁止打印连接成功与失败情况的日志，请使用 [overrideLogger] 覆写日志记录器
     */
    public fun noPrintInfo(): BotBuilder = apply {
        this.printInfo = false
    }

    /**
     * 禁止 Overflow 调用 Tencent 的网络接口
     *
     * 调用这些网络接口需要通过 `get_credentials` 获取凭证，再使用凭证访问接口。
     *
     * 部分 Onebot 实现不支持获取账号凭证，甚至于并不从QQ实现（如 Discord、Kook 等），连接这些实例前，需要禁用网络接口调用
     */
    public fun noPlatform(): BotBuilder = apply {
        this.noPlatform = true
    }

    /**
     * 发送消息时使用 CQ 码
     *
     * 即使不开启该选项，也可使用 CQ 码接收消息
     */
    public fun useCQCode(): BotBuilder = apply {
        this.useCQCode = true
    }

    /**
     * 覆写用于 Onebot 的日志记录器
     */
    public fun overrideLogger(logger: Logger): BotBuilder = apply {
        this.logger = logger
    }

    /**
     * 设置父工作，可选
     */
    public fun parentJob(job: Job?): BotBuilder = apply {
        this.parentJob = job
    }

    /**
     * 覆盖原有 BotConfiguration
     */
    @JvmSuppressWildcards
    public fun withBotConfiguration(supplier: Supplier<BotConfiguration>): BotBuilder = apply {
        this.configuration = supplier.get()
    }

    /**
     * 修改原有 BotConfiguration
     */
    @JvmSuppressWildcards
    public fun modifyBotConfiguration(consumer: Consumer<BotConfiguration>): BotBuilder = apply {
        consumer.accept(configuration)
    }

    /**
     * 设置或取消设置工作目录
     * @param block 传入QQ号，返回路径
     */
    @JvmSuppressWildcards
    public fun workingDir(block: (Long.() -> File)?): BotBuilder = apply {
        this.workingDir = block
    }

    /**
     * 连接到 Onebot
     *
     * @return 连接失败时，将返回 null
     */
    @JvmBlockingBridge
    public suspend fun connect(): Bot? {
        return OverflowAPI.get().botStarter.start(
            url = url,
            reversedPort = reversedPort,
            token = token,
            retryTimes = retryTimes,
            retryWaitMills = retryWaitMills,
            retryRestMills = retryRestMills,
            printInfo = printInfo,
            noPlatform = noPlatform,
            useCQCode = useCQCode,
            logger = logger,
            parentJob = parentJob,
            configuration = configuration,
            workingDir = workingDir,
        )
    }

    companion object {
        /**
         * 正向 WebSocket 连接
         */
        @JvmStatic
        fun positive(host: String): BotBuilder = BotBuilder().also { it.url = host }

        /**
         * 反向 WebSocket 连接
         */
        @JvmStatic
        fun reversed(port: Int): BotBuilder {
            if (port !in 1..65535) throw IllegalStateException("无效的反向 WebSocket 端口号")
            return BotBuilder().also { it.reversedPort = port }
        }
    }
}

interface IBotStarter {
    suspend fun start(
        url: String,
        reversedPort: Int,
        token: String,
        retryTimes: Int,
        retryWaitMills: Long,
        retryRestMills: Long,
        printInfo: Boolean,
        noPlatform: Boolean,
        useCQCode: Boolean,
        logger: Logger?,
        parentJob: Job?,
        configuration: BotConfiguration,
        workingDir: (Long.() -> File)?
    ): Bot?
}

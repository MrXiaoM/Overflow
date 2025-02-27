package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URI

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/1 17:01
 * Version: 1.0
 */
/**
 *
 * @param config 配置
 */
class ConnectFactory private constructor(
    private val config: BotConfig,
    private val parent: Job?,
    private val logger: Logger,
) {
    private val actionHandler: ActionHandler = ActionHandler(parent, logger)

    /**
     * 创建websocket客户端(支持cqhttp和mirai类型)
     * @return 连接实例
     */
    @JvmOverloads
    @Deprecated("please use build()")
    suspend fun createWebsocketClient(scope: CoroutineScope = CoroutineScope(CoroutineName("WSClient"))): WSClient? {
        val builder = StringBuilder()
        val header = mutableMapOf<String, String>()
        var ws: WSClient? = null
        builder.append(config.url)
        if (config.isAccessToken) {
            builder.append("?access_token=")
            builder.append(config.token)
            header["Authorization"] = "Bearer ${config.token}"
        }

        val url = builder.toString()
        try {
            ws = WSClient.createAndConnect(
                scope,
                config,
                URI.create(url),
                logger,
                actionHandler,
                config.retryTimes,
                config.retryWaitMills,
                config.retryRestMills,
                header
            )
        } catch (e: Exception) {
            if (e is CancellationException) {
                logger.info("▌ 连接请求已取消 ┈━═☆")
            } else {
                logger.error("▌ {} 连接错误，请检查服务端是否开启 ┈━═☆", url)
                logger.error("▌ 错误信息: {}: {}", e.javaClass.simpleName, e.message)
            }
        }
        return ws
    }

    /**
     * 创建反向Websocket服务端，并等待连接
     * @return 连接实例
     */
    @JvmOverloads
    @Deprecated("please use build()")
    suspend fun createWebsocketServerAndWaitConnect(
        scope: CoroutineScope = CoroutineScope(CoroutineName("WSServer"))
    ): Pair<WSServer, Bot>? {
        var pair: Pair<WSServer, Bot>? = null
        try {
            val address = InetSocketAddress(config.reversedPort)
            pair = WSServer.createAndWaitConnect(scope, config, address, logger, actionHandler, config.token)
        } catch (e: Exception) {
            if (e is CancellationException) {
                logger.info("▌ 反向 WebSocket 服务端被用户主动关闭")
            } else {
                logger.error("▌ 反向 WebSocket 绑定端口 {} 或连接客户端时出现错误 ┈━═☆", config.reversedPort)
                logger.error(e.stackTraceToString())
            }
        }
        return pair
    }

    @JvmOverloads
    fun createProducer(
        scope0: CoroutineScope = CoroutineScope(CoroutineName("ConnectFactory"))
    ): OneBotProducer {
        val scope = if (parent == null) scope0 else scope0 + parent
        if (config.isInReverseMode) {
            val address = InetSocketAddress(config.reversedPort)
            return ReversedOneBotProducer(WSServer.create(scope, config, address, logger, actionHandler, config.token))
        }
        //使用正向包装
        val builder = StringBuilder()
        val header = mutableMapOf<String, String>()
        builder.append(config.url)
        if (config.isAccessToken) {
            builder.append("?access_token=")
            builder.append(config.token)
            header["Authorization"] = "Bearer ${config.token}"
        }

        val url = builder.toString()

        return PositiveOneBotProducer(WSClient.create(
            scope,
            config,
            URI.create(url),
            logger,
            actionHandler,
            config.retryTimes,
            config.retryWaitMills,
            config.retryRestMills,
            header
        ))
    }


    companion object {
        @JvmStatic
        fun create(config: BotConfig, parent: Job? = null, logger: Logger = LoggerFactory.getLogger("Onebot")): ConnectFactory {
            return ConnectFactory(config, parent, logger)
        }
    }
}
enum class CloseCode(val code: Int) {
    NORMAL(1000),
    GOING_AWAY(1001),
    PROTOCOL_ERROR(1002),
    REFUSE(1003),
    NO_CODE(1005),
    ABNORMAL_CLOSE(1006),
    NO_UTF8(1007),
    POLICY_VALIDATION(1008),
    TOO_BIG(1009),
    EXTENSION(1010),
    UNEXPECTED_CONDITION(1011),
    SERVICE_RESTART(1012),
    TRY_AGAIN_LATER(1013),
    BAD_GATEWAY(1014),
    TLS_ERROR(1015),
    NEVER_CONNECTED(-1),
    BUGGY_CLOSE(-2),
    FLASH_POLICY(-3);

    override fun toString(): String {
        return name.uppercase()
    }

    companion object {
        fun valueOf(code: Int): CloseCode? {
            return values().firstOrNull { it.code == code }
        }
    }
}
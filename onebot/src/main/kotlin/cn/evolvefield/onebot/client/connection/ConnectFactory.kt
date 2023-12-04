package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
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
    private val logger: Logger,
) {
    private val actionHandler: ActionHandler = ActionHandler(logger)

    /**
     * 创建websocket客户端(支持cqhttp和mirai类型)
     * @return 连接实例
     */
    @JvmOverloads
    fun createWebsocketClient(scope: CoroutineScope = CoroutineScope(CoroutineName("WSClient"))): WSClient? {
        val builder = StringBuilder()
        val header = mutableMapOf<String, String>()
        var ws: WSClient? = null
        if (config.miraiHttp) {
            builder.append(config.url)
            builder.append("/all")
            builder.append("?verifyKey=")
            if (config.isAccessToken) {
                builder.append(config.token)
            }
            builder.append("&qq=")
            builder.append(config.botId)
        } else {
            builder.append(config.url)
            if (config.isAccessToken) {
                builder.append("?access_token=")
                builder.append(config.token)
                header["Authorization"] = "Bearer ${config.token}"
            }
        }
        val url = builder.toString()
        try {
            ws = WSClient.createAndConnect(scope, URI.create(url), logger, actionHandler, header)
        } catch (e: Exception) {
            logger.error("▌ {} 连接错误，请检查服务端是否开启 ┈━═☆", url)
        }
        return ws
    }

    /**
     * 创建反向Websocket服务端，并等待连接
     * @return 连接实例
     */
    @JvmOverloads
    suspend fun createWebsocketServerAndWaitConnect(scope: CoroutineScope = CoroutineScope(CoroutineName("WSServer"))): Pair<WSServer, Bot>? {
        var pair: Pair<WSServer, Bot>? = null
        try {
            val address = InetSocketAddress(config.reversedPort)
            pair = WSServer.createAndWaitConnect(scope, address, logger, actionHandler)
        } catch (e: Exception) {
            logger.error("▌ 反向 WebSocket 绑定端口 {} 或连接客户端时出现错误 ┈━═☆", config.reversedPort)
            logger.error(e.stackTraceToString())
        }
        return pair
    }

    companion object {
        @JvmStatic
        fun create(config: BotConfig, logger: Logger = LoggerFactory.getLogger("Onebot")): ConnectFactory {
            return ConnectFactory(config, logger)
        }
    }
}
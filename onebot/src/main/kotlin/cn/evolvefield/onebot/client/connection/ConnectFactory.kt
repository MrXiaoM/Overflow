package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.client.handler.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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
 * @param channel 队列消息
 */
class ConnectFactory(
    private val config: BotConfig,
    private val channel: Channel<String> = Channel()
) {
    private val actionHandler: ActionHandler = ActionHandler()
    var ws: WSClient? = kotlin.runCatching {
        createWebsocketClient()
    }.onFailure {
        WSClient.log.error("▌ §c连接错误，请检查服务端是否开启 §a┈━═☆")
    }.getOrNull()

    /**
     * 创建websocket客户端(支持cqhttp和mirai类型)
     * @return 连接示例
     */
    fun createWebsocketClient(): WSClient? {
        val builder = StringBuilder()
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
            }
        }
        val url = builder.toString()
        try {
            ws = WSClient(URI.create(url), channel, actionHandler)
            ws.connect()
        } catch (e: Exception) {
            WSClient.log.error("▌ §c{}连接错误，请检查服务端是否开启 §a┈━═☆", url)
        }
        return ws
    }

    fun createEventBus(scope: CoroutineScope): EventBus {
        return EventBus.create(scope, channel)
    }

    fun stop() {
        ws!!.close()
    }
}
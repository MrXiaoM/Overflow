package cn.evolvefield.onebot.client.connection

import cn.evole.onebot.sdk.util.json.JsonsObject
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * Project: onebot-client
 * Author: cnlimiter
 * Date: 2023/4/4 2:20
 * Description:
 */
class WSClient(uri: URI, private val channel: Channel<String>, private val actionHandler: ActionHandler) :
    WebSocketClient(uri) {
    val def = CompletableDeferred<Boolean>()
    fun createBot(): Bot {
        return Bot(this, actionHandler)
    }

    private suspend fun await(): WSClient? {
        connect()
        return if (def.await()) {
            this
        } else {
            null
        }
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        log.info("▌ 已连接到服务器 ┈━═☆")
        if (def.isActive) def.complete(true)
    }

    override fun onMessage(message: String) {
        try {
            val jsonObject = JsonsObject(message)
            if (HEART_BEAT != jsonObject.optString(META_EVENT)) { //过滤心跳
                log.debug("接收到原始消息{}", jsonObject.toString())
                if (jsonObject.has(API_RESULT_KEY)) {
                    if (FAILED_STATUS == jsonObject.optString(RESULT_STATUS_KEY)) {
                        log.debug("请求失败: {}", jsonObject.optString("wording"))
                    } else actionHandler.onReceiveActionResp(jsonObject) //请求执行
                } else if (!channel.trySend(message).isSuccess) { //事件监听
                    log.error("监听错误: {}", message)
                }
            }
        } catch (e: JsonSyntaxException) {
            log.error("Json语法错误:{}", message)
        }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        log.info("▌ 服务器因 {} 已关闭", reason)
        if (def.isActive) def.complete(false)
    }

    override fun onError(ex: Exception) {
        log.error("▌ 出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger("WSClient")
        private const val META_EVENT = "meta_event_type"
        private const val API_RESULT_KEY = "echo"
        private const val FAILED_STATUS = "failed"
        private const val RESULT_STATUS_KEY = "status"
        private const val HEART_BEAT = "heartbeat"
        private const val LIFE_CYCLE = "lifecycle"

        suspend fun createAndConnect(uri: URI, channel: Channel<String>, actionHandler: ActionHandler): WSClient? {
            val ws = WSClient(uri, channel, actionHandler)
            return ws.await()
        }
    }
}
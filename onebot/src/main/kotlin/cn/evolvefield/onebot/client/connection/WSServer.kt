package cn.evolvefield.onebot.client.connection

import cn.evole.onebot.sdk.util.json.JsonsObject
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.util.ActionSendUtils
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URI
import kotlin.coroutines.CoroutineContext

/**
 * Project: onebot-client
 * Author: MrXiaoM
 * Date: 2023/12/4 8:36
 * Description:
 */
class WSServer(
    private val scope: CoroutineScope,
    address: InetSocketAddress,
    private val logger: Logger,
    private val actionHandler: ActionHandler
) : WebSocketServer(address) {
    private var eventBus: EventBus? = null
    val def = CompletableDeferred<Bot>()

    fun createEventBus(): EventBus {
        return if (eventBus != null) eventBus!! else EventBus().also { eventBus = it }
    }

    override fun onStart() {
        logger.info("▌ 启动反向WebSocket服务端 $address")
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        logger.info("▌ 反向WebSocket ${conn.remoteSocketAddress} 已连接 ┈━═☆")
        def.complete(Bot(conn, actionHandler))
    }

    override fun onMessage(conn: WebSocket, message: String) {
        try {
            val jsonObject = JsonsObject(message)
            if (HEART_BEAT != jsonObject.optString(META_EVENT)) { //过滤心跳
                logger.debug("Client received <-- {}", jsonObject.toString())
                if (jsonObject.has(API_RESULT_KEY)) {
                    actionHandler.onReceiveActionResp(jsonObject) //请求执行
                } else {
                    scope.launch {
                        mutex.withLock {
                            eventBus?.onReceive(message)
                        }
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            logger.error("Json语法错误: {}", message)
        }
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        logger.info("▌ 反向WebSocket ${conn.remoteSocketAddress} 连接因 {} 已关闭", reason)
        eventBus?.stop()
        if (mutex.isLocked) mutex.unlock()
        if (ActionSendUtils.mutex.isLocked) ActionSendUtils.mutex.unlock()
    }

    override fun onError(conn: WebSocket, ex: Exception) {
        logger.error("▌ 反向WebSocket ${conn.remoteSocketAddress} 出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    companion object {
        private const val META_EVENT = "meta_event_type"
        private const val API_RESULT_KEY = "echo"
        private const val HEART_BEAT = "heartbeat"

        val mutex = Mutex()
        suspend fun createAndWaitConnect(scope: CoroutineScope, address: InetSocketAddress, logger: Logger, actionHandler: ActionHandler): Pair<WSServer, Bot> {
            val ws = WSServer(scope, address, logger, actionHandler)
            ws.start()
            return ws to ws.def.await()
        }
    }
}

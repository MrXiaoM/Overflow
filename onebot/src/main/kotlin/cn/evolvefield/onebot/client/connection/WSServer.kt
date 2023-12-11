package cn.evolvefield.onebot.client.connection

import cn.evole.onebot.sdk.util.json.JsonsObject
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.util.ActionSendUtils
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.java_websocket.WebSocket
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import java.net.InetSocketAddress

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
    private val actionHandler: ActionHandler,
    private val token: String
) : WebSocketServer(address) {
    private var eventBus: EventBus? = null
    private var bot: Bot? = null
    val def = CompletableDeferred<Bot>()

    fun createEventBus(): EventBus {
        return if (eventBus != null) eventBus!! else EventBus().also { eventBus = it }
    }

    override fun onStart() {
        logger.info("▌ 反向 WebSocket 服务端已在 $address 启动")
        logger.info("▌ 正在等待客户端连接...")
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        if (bot?.channel?.isOpen == true) {
            conn.close(CloseFrame.NORMAL, "Overflow 不支持多客户端连接")
            return
        }
        if (token.isNotBlank()) {
            if (handshake.hasFieldValue("Authorization")) {
                val param = handshake.getFieldValue("Authorization").removePrefix("Bearer ")
                if (param != token) {
                    conn.close(CloseFrame.NORMAL, "token 错误")
                    return
                }
            } else if (handshake.resourceDescriptor.contains("access_token=")) {
                val param = handshake.resourceDescriptor.substringAfter("access_token=").substringBefore("&")
                if (param != token) {
                    conn.close(CloseFrame.NORMAL, "token 错误")
                    return
                }
            } else {
                conn.close(CloseFrame.NORMAL, "未提供 token")
                return
            }
        }
        logger.info("▌ 反向 WebSocket 客户端 ${conn.remoteSocketAddress} 已连接 ┈━═☆")
        (bot ?: Bot(conn, actionHandler).also {
            bot = it
            def.complete(it)
        }).conn = conn
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
        logger.info("▌ 反向 WebSocket 客户端连接因 {} 已关闭", reason)
        if (mutex.isLocked) mutex.unlock()
        if (ActionSendUtils.mutex.isLocked) ActionSendUtils.mutex.unlock()
    }

    override fun onError(conn: WebSocket, ex: Exception) {
        logger.error("▌ 反向 WebSocket 客户端连接出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    companion object {
        private const val META_EVENT = "meta_event_type"
        private const val API_RESULT_KEY = "echo"
        private const val HEART_BEAT = "heartbeat"

        val mutex = Mutex()
        suspend fun createAndWaitConnect(scope: CoroutineScope, address: InetSocketAddress, logger: Logger, actionHandler: ActionHandler, token: String): Pair<WSServer, Bot> {
            val ws = WSServer(scope, address, logger, actionHandler, token)
            ws.start()
            return ws to ws.def.await()
        }
    }
}

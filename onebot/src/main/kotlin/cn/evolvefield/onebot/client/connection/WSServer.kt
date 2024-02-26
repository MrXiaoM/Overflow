package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
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
    override val scope: CoroutineScope,
    address: InetSocketAddress,
    override val logger: Logger,
    override val actionHandler: ActionHandler,
    private val token: String
) : WebSocketServer(address), IAdapter {
    private var bot: Bot? = null
    val def = CompletableDeferred<Bot>()

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
                val param = handshake.getFieldValue("Authorization").run {
                    if (lowercase().startsWith("bearer ")) substring(7) else this
                }
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

    override fun onMessage(conn: WebSocket, message: String) = onReceiveMessage(message)

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        logger.info(
            "▌ 反向 WebSocket 客户端连接因 {} 已关闭 (关闭码: {})",
            reason.ifEmpty { "未知原因" },
            CloseCode.valueOf(code) ?: code
        )
        unlockMutex()
    }

    override fun onError(conn: WebSocket, ex: Exception) {
        logger.error("▌ 反向 WebSocket 客户端连接出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    companion object {
        suspend fun createAndWaitConnect(scope: CoroutineScope, address: InetSocketAddress, logger: Logger, actionHandler: ActionHandler, token: String): Pair<WSServer, Bot> {
            val ws = WSServer(scope, address, logger, actionHandler, token)
            ws.start()
            return ws to ws.def.await()
        }
    }
}

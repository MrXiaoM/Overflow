package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import kotlinx.coroutines.*
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
    private val config: BotConfig,
    address: InetSocketAddress,
    override val logger: Logger,
    override val actionHandler: ActionHandler,
    private val token: String
) : WebSocketServer(address), IAdapter {
    private var bot: Bot? = null
    val connectDef = CompletableDeferred<Bot>(config.parentJob)

    override fun onStart() {
        logger.info("▌ 反向 WebSocket 服务端已在 $address 启动")
        logger.info("▌ 正在等待客户端连接...")
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        if (handshake.resourceDescriptor != "/") return
        if (bot?.channel?.isOpen == true) {
            conn.close(CloseFrame.NORMAL, "Overflow 的反向 WS 适配器暂不支持多客户端连接")
            return
        }
        if (token.isNotBlank()) {
            if (handshake.hasFieldValue("Authorization")) {
                val param = handshake.getFieldValue("Authorization").run {
                    if (lowercase().startsWith("bearer ")) substring(7) else this
                }
                if (param != token) {
                    conn.close(CloseFrame.NORMAL, "客户端提供的 token 错误")
                    return
                }
            } else if (handshake.resourceDescriptor.contains("access_token=")) {
                val param = handshake.resourceDescriptor.substringAfter("access_token=").substringBefore("&")
                if (param != token) {
                    conn.close(CloseFrame.NORMAL, "客户端提供的 token 错误")
                    return
                }
            } else {
                conn.close(CloseFrame.NORMAL, "客户端未提供 token")
                return
            }
        }
        logger.info("▌ 反向 WebSocket 客户端 ${conn.remoteSocketAddress} 已连接 ┈━═☆")
        bot = Bot(conn, config, actionHandler).also { it.conn = conn }
        connectDef.complete(bot!!)
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
        suspend fun createAndWaitConnect(
            scope: CoroutineScope, config: BotConfig,
            address: InetSocketAddress, logger: Logger,
            actionHandler: ActionHandler, token: String
        ): Pair<WSServer, Bot> {
            val ws = WSServer(scope, config, address, logger, actionHandler, token).also { it.start() }
            val bot = ws.connectDef.await()
            return ws to bot
        }
    }
}

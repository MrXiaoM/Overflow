package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.java_websocket.WebSocket
import org.java_websocket.WebSocketServerFactory
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.DefaultWebSocketServerFactory
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import java.net.InetSocketAddress
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Project: onebot-client
 * Author: MrXiaoM
 * Date: 2023/12/4 8:36
 * Description:
 */
val random = Random(System.currentTimeMillis())
class WSServer(
    override val scope: CoroutineScope,
    private val config: BotConfig,
    address: InetSocketAddress,
    override val logger: Logger,
    override val actionHandler: ActionHandler,
    private val token: String
) : WebSocketServer(address), IAdapter {
    data class ConnectionBox(
        val bot: Bot
    ) {
        private val hashCode by lazy {
            random.nextInt()
        }
        override fun equals(other: Any?): Boolean {
            return hashCode() == other?.hashCode()
        }

        override fun hashCode(): Int {
            return hashCode
        }
    }

    internal var botConsumer: suspend (Bot) -> Unit = {}
    private val bots: MutableList<Bot> = mutableListOf()
    // 通过使 ConnectionBox 互不相等，使得当 bot 重新上线时，botChannel 推送成功。
    private val botChannel = Channel<ConnectionBox>()
    private val muteX = Mutex()
    val connectDef = CompletableDeferred<Bot>(config.parentJob)

    val closeHandler = mutableListOf<() -> Unit>()

    init {
        connectionLostTimeout = Math.max(0, config.heartbeatCheckSeconds)
        //等待测试
        setWebSocketFactory(object : WebSocketServerFactory by DefaultWebSocketServerFactory() {
            override fun close() {
                for (handler in closeHandler) {
                    handler()
                }
            }
        })
    }

    override fun onStart() {
        logger.info("▌ 反向 WebSocket 服务端已在 $address 启动")
        logger.info("▌ 正在等待客户端连接...")
    }

    suspend fun awaitNewBot(timeout: Duration = Duration.INFINITE): Bot {
        return withTimeout(timeout) {
            botChannel.receive().bot
        }
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        val split = handshake.resourceDescriptor.split('?', limit=2).toMutableList().run {
            if (!this[0].startsWith("/")) {
                this[0] = "/" + this[0]
            }
            toList()
        }
        if (split[0] != "/") {
            conn.close(CloseFrame.NORMAL, "path ${split[0]} not found")
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
            } else if (split.size > 1 && split[1].contains("access_token=")) {
                val param = split[1].substringAfter("access_token=").substringBefore("&")
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
        scope.launch {
            val bot = muteX.withLock {
                Bot(conn, config, actionHandler).also { it.conn = conn }.apply {
                    connectDef.complete(this)
                }
            }
            bots.add(bot)
            botConsumer.invoke(bot)
            botChannel.send(ConnectionBox(bot))
        }
    }

    override fun onMessage(conn: WebSocket, message: String) = onReceiveMessage(message)

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        logger.info(
            "▌ 反向 WebSocket 客户端连接因 {} 已关闭 (关闭码: {})",
            reason.ifEmpty { "未知原因" },
            CloseCode.valueOf(code) ?: code
        )
        unlockMutex()
        scope.launch {
            muteX.withLock {
                if (!bots.removeIf { it.conn == conn }) {
                    logger.warn("bot移除失败，这并不应该被发生。")
                }
            }
        }
    }

    //ws连接阶段时conn为null
    override fun onError(conn: WebSocket?, ex: Exception) {
        logger.error("▌ 反向 WebSocket 客户端连接出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    companion object {
        @Deprecated("please use create() and awaitNewBot()")
        suspend fun createAndWaitConnect(
            scope: CoroutineScope, config: BotConfig,
            address: InetSocketAddress, logger: Logger,
            actionHandler: ActionHandler, token: String
        ): Pair<WSServer, Bot> {
            val ws = WSServer(scope, config, address, logger, actionHandler, token).also { it.start() }
            val bot = ws.connectDef.await()
            return ws to bot
        }

        fun create(
            scope: CoroutineScope, config: BotConfig,
            address: InetSocketAddress, logger: Logger,
            actionHandler: ActionHandler, token: String
        ): WSServer {
            val ws = WSServer(scope, config, address, logger, actionHandler, token).also { it.start() }
            return ws
        }
    }
}

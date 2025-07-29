package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.client.handler.EventHolder
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.slf4j.Logger
import java.net.URI

/**
 * Project: onebot-client
 * Author: cnlimiter
 * Date: 2023/4/4 2:20
 * Description:
 */
internal class WSClient(
    override val scope: CoroutineScope,
    private val config: BotConfig,
    uri: URI,
    override val logger: Logger,
    override val actionHandler: ActionHandler,
    private val retryTimes: Int = 5,
    private val retryWaitMills: Long = 5000L,
    private val retryRestMills: Long = 60000L,
    header: Map<String, String> = mapOf(),
) : WebSocketClient(uri, header), IAdapter {
    internal var botConsumer: suspend (Bot) -> Unit = {}
    override val eventsHolder: MutableMap<Long, MutableList<EventHolder>> = mutableMapOf()
    private var retryCount = 0
    private var scheduleClose = false

    init {
        connectionLostTimeout = Math.max(0, config.heartbeatCheckSeconds)
        @OptIn(InternalCoroutinesApi::class)
        config.parentJob?.run { Job(this) }?.invokeOnCompletion(
            onCancelling = true,
            invokeImmediately = true,
        ) { if (isOpen) close() }
    }

    private val connectDef = CompletableDeferred<Boolean>(config.parentJob)

    suspend fun createBot(): Bot {
        val bot = Bot(this, this, config, actionHandler)
        botConsumer.invoke(bot)
        return bot
    }

    suspend fun connectSuspend(): Boolean {
        if (super.connectBlocking()) return true
        return connectDef.await()
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        logger.info("▌ 已连接到服务器 ┈━═☆")
    }

    override fun connect() {
        scheduleClose = false
        super.connect()
    }

    override fun close() {
        scheduleClose = true
        super.close()
    }

    override fun onMessage(message: String) = onReceiveMessage(message)

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        logger.info(
            "▌ 服务器连接因 {} 已关闭 (关闭码: {})",
            reason.ifEmpty { "未知原因" },
            CloseCode.valueOf(code) ?: code
        )
        unlockMutex()

        // 自动重连
        if (!scheduleClose) retry()
    }

    private fun retry() {
        if (retryTimes < 1 || retryWaitMills < 0) {
            logger.warn("连接失败，未开启自动重连，放弃连接")
            connectDef.complete(false)
            return
        }
        scope.launch {
            if (retryCount < retryTimes) {
                retryCount++
                logger.warn(
                    "等待 ${
                        String.format(
                            "%.1f",
                            retryWaitMills / 1000.0F
                        )
                    } 秒后重连 (第 $retryCount/$retryTimes 次)"
                )
                delay(retryWaitMills)
            } else {
                retryCount = 0
                if (retryRestMills < 0) {
                    logger.warn("重连次数耗尽... 放弃重试")
                    return@launch
                }
                logger.warn("重连次数耗尽... 休息 ${String.format("%.1f", retryRestMills / 1000.0F)} 秒后重试")
                delay(retryRestMills)
            }
            logger.info("正在重连...")
            if (reconnectBlocking()) {
                retryCount = 0
                connectDef.complete(true)
            }
        }
    }

    override fun onError(ex: Exception) {
        logger.error("▌ 出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    companion object {
        suspend fun createAndConnect(
            scope: CoroutineScope,
            config: BotConfig,
            uri: URI,
            logger: Logger,
            actionHandler: ActionHandler,
            retryTimes: Int,
            retryWaitMills: Long,
            retryRestMills: Long,
            header: Map<String, String> = mapOf()
        ): WSClient? {
            val ws =
                WSClient(scope, config, uri, logger, actionHandler, retryTimes, retryWaitMills, retryRestMills, header)
            return ws.takeIf { ws.connectSuspend() }
        }

        fun create(
            scope: CoroutineScope,
            config: BotConfig,
            uri: URI,
            logger: Logger,
            actionHandler: ActionHandler,
            retryTimes: Int,
            retryWaitMills: Long,
            retryRestMills: Long,
            header: Map<String, String> = mapOf()
        ): WSClient {
            return WSClient(
                scope,
                config,
                uri,
                logger,
                actionHandler,
                retryTimes,
                retryWaitMills,
                retryRestMills,
                header
            )
        }
    }
}

package cn.evolvefield.onebot.client.connection

import cn.evole.onebot.sdk.util.json.JsonsObject
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.util.ActionSendUtils
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ServerHandshake
import org.slf4j.Logger
import java.net.URI
import kotlin.math.log

/**
 * Project: onebot-client
 * Author: cnlimiter
 * Date: 2023/4/4 2:20
 * Description:
 */
class WSClient(
    private val scope: CoroutineScope,
    uri: URI,
    private val logger: Logger,
    private val actionHandler: ActionHandler,
    val retryTimes: Int = 5,
    val retryWaitMills: Long = 5000L,
    val retryRestMills: Long = 60000L,
    header: Map<String, String> = mapOf(),
) : WebSocketClient(uri, header) {
    private var retryCount = 0
    private var eventBus: EventBus? = null
    val connectDef = CompletableDeferred<Boolean>()
    fun createBot(): Bot {
        return Bot(this, actionHandler)
    }

    fun createEventBus(): EventBus {
        return if (eventBus != null) eventBus!! else EventBus().also { eventBus = it }
    }

    suspend fun connectSuspend(): Boolean {
        if (super.connectBlocking()) return true
        return connectDef.await()
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        logger.info("▌ 已连接到服务器 ┈━═☆")
    }

    override fun onMessage(message: String) {
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
            logger.error("Json语法错误:{}", message)
        }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        logger.info(
            "▌ 服务器连接因 {} 已关闭 (关闭码: {})",
            reason.ifEmpty { "未知原因" },
            CloseCode.valueOf(code) ?: code
        )
        runCatching {
            if (mutex.isLocked) mutex.unlock()
            if (ActionSendUtils.mutex.isLocked) ActionSendUtils.mutex.unlock()
        }
        if (code != CloseFrame.NORMAL) { // TODO: 测试确认异常关闭码
            if (retryTimes < 1) {
                connectDef.complete(false)
                return
            }
            scope.launch {
                if (retryCount < retryTimes) {
                    retryCount++
                    logger.warn("等待 ${String.format("%.1f", retryWaitMills / 1000.0F)} 秒后重连 (第 $retryCount/$retryTimes 次")
                    delay(retryWaitMills)
                } else {
                    retryCount = 0
                    logger.warn("重连次数耗尽... 休息 ${String.format("%.1f", retryRestMills / 1000.0F)} 秒后重试")
                    delay(retryRestMills)
                }
                logger.info("正在重连...")
                if (reconnectBlocking()) {
                    connectDef.complete(true)
                }
            }
        }
    }

    override fun onError(ex: Exception) {
        logger.error("▌ 出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    companion object {
        private const val META_EVENT = "meta_event_type"
        private const val API_RESULT_KEY = "echo"
        private const val HEART_BEAT = "heartbeat"

        val mutex = Mutex()
        fun createAndConnect(scope: CoroutineScope, uri: URI, logger: Logger, actionHandler: ActionHandler, retryTimes: Int, retryWaitMills: Long, retryRestMills: Long, header: Map<String, String> = mapOf()): WSClient? {
            val ws = WSClient(scope, uri, logger, actionHandler, retryTimes, retryWaitMills, retryRestMills, header)
            return ws.takeIf { runBlocking { ws.connectSuspend() } }
        }
    }
}

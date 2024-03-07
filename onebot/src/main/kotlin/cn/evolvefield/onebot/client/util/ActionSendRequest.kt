package cn.evolvefield.onebot.client.util

import cn.evole.onebot.sdk.util.json.JsonsObject
import cn.evolvefield.onebot.client.core.Bot
import com.google.gson.JsonObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.java_websocket.WebSocket
import org.slf4j.Logger

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:06
 * Version: 1.0
 */
/**
 * @param channel        [WebSocket]
 * @param requestTimeout Request Timeout
 */
class ActionSendRequest(
    private val bot: Bot,
    private val logger: Logger,
    private val channel: WebSocket,
    private val requestTimeout: Long
) {
    private val resp = CompletableDeferred<JsonsObject>()
    //private var resp: JsonsObject? = null
    /**
     * @param req Request json data
     * @return Response json data
     */
    @Throws(TimeoutCancellationException::class, ActionFailedException::class)
    suspend fun send(req: JsonObject): JsonsObject {
        val resp = mutex.withLock {
            kotlin.runCatching {
                withTimeout(requestTimeout) {
                    logger.debug("Send to server --> {}", req.toString())
                    channel.send(req.toString())
                    resp.await()
                }
            }.onFailure { resp.cancel() }.getOrThrow()
        }
        if (resp.optString("status") == "failed") {
            val ret = resp.optInt("retcode").takeIf { it != 0 }?.run { ", retCode=$this" } ?: ""
            throw ActionFailedException(
                app = "${bot.appName} v${bot.appVersion}",
                msg = "${resp.optString("message")}$ret",
                json = resp
            )
        }
        return resp
        //synchronized(this) { this.wait(requestTimeout) }
        //return resp
    }

    /**
     * @param resp Response json data
     */
    fun onCallback(resp: JsonsObject) {
        this.resp.complete(resp)
        //this.resp = resp
        //synchronized(this) { this.notify() }
    }

    companion object {
        val mutex = Mutex()
    }
}

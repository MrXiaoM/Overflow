package cn.evolvefield.onebot.client.util

import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.client.core.Bot
import com.google.gson.JsonObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.java_websocket.WebSocket
import org.slf4j.Logger
import java.util.*

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
    private val resp = CompletableDeferred<JsonObject>()
    /**
     * @param req Request json data
     * @return Response json data
     */
    @Throws(TimeoutCancellationException::class, ActionFailedException::class)
    suspend fun send(req: JsonObject): JsonObject {
        val resp = mutex.withLock {
            kotlin.runCatching {
                withTimeout(requestTimeout) {
                    logger.debug("[Send] --> {}", req.toString())
                    channel.send(req.toString())
                    resp.await()
                }
            }.onFailure { resp.cancel() }.getOrThrow()
        }
        if (resp.get("status").asString == "failed") {
            val extra = run {
                req["params"]?.asJsonObject?.also { params ->
                    params["message"]?.asJsonArray?.also { messages ->
                        val extraFileTypes = messages.filter {
                            listOf("image", "record", "video").contains(it.asJsonObject?.get("type")?.asString)
                                    && it.asJsonObject?.has("file") == true
                        }.mapNotNull {
                            val file = it.asJsonObject!!["file"].asString
                            if (file.startsWith("base64://")) {
                                val bytes = Base64.getDecoder().decode(file.replace("base64://", ""))
                                "msgFileType=${bytes.fileType}"
                            } else null
                        }
                        if (extraFileTypes.isNotEmpty()) {
                            return@run extraFileTypes.joinToString(", ")
                        }
                    }
                }
                ""
            }
            throw ActionFailedException(
                app = "${bot.appName} v${bot.appVersion}",
                msg = "${resp.ignorable("message", "")}$extra",
                json = resp
            )
        }
        return resp
    }

    /**
     * @param resp Response json data
     */
    fun onCallback(resp: JsonObject) {
        this.resp.complete(resp)
    }

    companion object {
        val mutex = Mutex()
    }
}

package cn.evolvefield.onebot.client.util

import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.sdk.util.ignorableArray
import cn.evolvefield.onebot.sdk.util.ignorableObject
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.java_websocket.WebSocket
import org.slf4j.Logger
import top.mrxiaom.overflow.action.ActionContext
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
    private val context: ActionContext,
    parent: Job?,
    private val logger: Logger,
    private val channel: WebSocket,
    private val requestTimeout: Long
) {
    private val resp = CompletableDeferred<JsonObject>(parent)
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
        if (resp.ignorable("status", if (context.ignoreStatus) "" else "failed") == "failed") {
            val extra = runCatching {
                val params = req.ignorableObject("params") { JsonObject() }
                val messages = params.ignorableArray("message") { JsonArray() }
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
                return@runCatching if (extraFileTypes.isNotEmpty()) {
                    "; " + extraFileTypes.joinToString(", ")
                } else ""
            }.getOrElse { "" }
            throw ActionFailedException(
                action = context.action,
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

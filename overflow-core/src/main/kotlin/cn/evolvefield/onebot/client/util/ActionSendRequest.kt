package cn.evolvefield.onebot.client.util

import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.sdk.util.ignorableArray
import cn.evolvefield.onebot.sdk.util.ignorableObject
import cn.evolvefield.onebot.sdk.util.nullableInt
import cn.evolvefield.onebot.sdk.util.nullableString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
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
internal class ActionSendRequest(
    private val bot: Bot,
    private val context: ActionContext,
    parent: Job?,
    private val logger: Logger,
    private val channel: WebSocket,
    private val requestTimeout: Long
) {
    private val resp = CompletableDeferred<JsonObject>(parent)
    /**
     * 执行向 Onebot 服务端主动发送请求
     * @param req 发送的 JSON 数据
     * @return 收到的回应
     */
    @Throws(TimeoutCancellationException::class, ActionFailedException::class)
    suspend fun send(req: JsonObject): JsonObject {
        // 发送请求，并等待结果回调
        val resp = mutex.withLock {
            kotlin.runCatching {
                withTimeout(requestTimeout) {
                    val echo = req.nullableString("echo", null)
                    if (echo != null) {
                        logger.debug("[Send][$echo] --> {}", req.toString())
                    } else {
                        logger.debug("[Send] --> {}", req.toString())
                    }
                    channel.send(req.toString())
                    resp.await()
                }
            }.onFailure { resp.cancel() }.getOrThrow()
        }
        // 根据请求上下文，进行调用失败检查判定
        if (resp.ignorable("status", if (context.ignoreStatus) "" else "failed") == "failed") {
            // 如果发送的是消息，并且里面有 base64 资源，在报错信息增加文件类型信息
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
                msg = resp.ignorable("message", "") + extra,
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

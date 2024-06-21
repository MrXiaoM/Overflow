package cn.evolvefield.onebot.client.handler

import cn.evolvefield.onebot.sdk.action.ActionPath
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.util.ActionFailedException
import cn.evolvefield.onebot.client.util.ActionSendRequest
import cn.evolvefield.onebot.sdk.util.nullableString
import com.google.gson.JsonObject
import kotlinx.coroutines.Job
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:05
 * Version: 1.0
 */
class ActionHandler(
    private val parent: Job?,
    private val logger: Logger
) {
    /**
     * 请求回调数据
     */
    private val apiCallbackMap: MutableMap<String, ActionSendRequest> = ConcurrentHashMap()

    /**
     * 用于标识请求，可以是任何类型的数据，OneBot 将会在调用结果中原样返回
     */
    private var echo = 0L

    /**
     * 处理响应结果
     *
     * @param respJson 回调结果
     */
    fun onReceiveActionResp(respJson: JsonObject) {
        respJson.nullableString("echo", null)?.also { echo ->
            // 唤醒挂起的协程
            apiCallbackMap.remove(echo)?.onCallback(respJson) ?: run {
                logger.warn("收到了未知的 action 回应: $respJson")
            }
        }
    }

    /**
     * @param bot     Session
     * @param action  请求路径
     * @param params  请求参数
     * @return 请求结果
     */
    suspend fun action(bot: Bot, action: ActionPath, params: JsonObject?): JsonObject {
        if (!bot.channel.isOpen) {
            return JsonObject().apply {
                addProperty("status", "failed")
                addProperty("retcode", -1)
                addProperty("message", "WebSocket channel is not opened")
            }
        }
        val request = ActionSendRequest(bot, parent, logger, bot.channel, timeout)
        val reqJson = generateReqJson(action, params) { echo ->
            apiCallbackMap[echo] = request
        }
        return try {
            request.send(reqJson)
        } catch (e: Exception) {
            logger.warn("请求失败: [${action.path}] ${e.message}。如果你认为这是 Overflow 的问题，请带上 logs/onebot 中的日志来反馈。")
            logger.trace("Stacktrace: ", e)
            if (e is ActionFailedException) e.json
            else JsonObject().apply {
                addProperty("status", "failed")
                addProperty("retcode", -1)
                addProperty("message", e.message ?: "")
            }
        }
    }

    /**
     * 构建请求数据
     * {"action":"send_private_msg","params":{"user_id":10001000,"message":"你好"},"echo":"123"}
     *
     * @param action 请求路径
     * @param params 请求参数
     * @return 请求数据结构
     */
    private fun generateReqJson(
        action: ActionPath,
        params: JsonObject?,
        block: (String) -> Unit = {}
    ): JsonObject {
        val json = JsonObject()
        json.addProperty("action", action.path)
        if (params != null) json.add("params", params)
        val echoLong = echo++
        json.addProperty("echo", echoLong)
        if (echo >= Long.MAX_VALUE) echo = 0 // reset echo
        block(echoLong.toString())
        return json
    }

    companion object {
        val timeout by lazy {
            System.getProperty("overflow.timeout")?.toLongOrNull() ?: 10000L
        }
    }
}
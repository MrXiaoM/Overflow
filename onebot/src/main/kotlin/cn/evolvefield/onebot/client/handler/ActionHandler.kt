package cn.evolvefield.onebot.client.handler

import cn.evole.onebot.sdk.action.ActionPath
import cn.evole.onebot.sdk.util.json.JsonsObject
import cn.evolvefield.onebot.client.util.ActionFailedException
import cn.evolvefield.onebot.client.util.ActionSendUtils
import com.google.gson.JsonObject
import org.java_websocket.WebSocket
import org.slf4j.Logger

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:05
 * Version: 1.0
 */
class ActionHandler(
    private val logger: Logger
) {
    /**
     * 请求回调数据
     */
    private val apiCallbackMap: MutableMap<String, ActionSendUtils> = HashMap()

    /**
     * 用于标识请求，可以是任何类型的数据，OneBot 将会在调用结果中原样返回
     */
    private var echo = 0

    /**
     * 处理响应结果
     *
     * @param respJson 回调结果
     */
    fun onReceiveActionResp(respJson: JsonsObject) {
        val echo = respJson.optString("echo")
        val actionSendUtils = apiCallbackMap[echo]
        if (actionSendUtils != null) {
            // 唤醒挂起的协程
            actionSendUtils.onCallback(respJson)
            apiCallbackMap.remove(echo)
        }
    }

    /**
     * @param channel Session
     * @param action  请求路径
     * @param params  请求参数
     * @return 请求结果
     */
    suspend fun action(channel: WebSocket, action: ActionPath, params: JsonObject?): JsonsObject {
        if (!channel.isOpen) {
            return JsonsObject(JsonObject().apply {
                addProperty("status", "failed")
                addProperty("retcode", -1)
                addProperty("message", "WebSocket channel is not opened")
            })
        }
        val reqJson = generateReqJson(action, params)
        val actionSendUtils = ActionSendUtils(logger, channel, timeout)
        val echo = reqJson["echo"].asString
        apiCallbackMap[echo] = actionSendUtils
        return try {
            actionSendUtils.send(reqJson)
        } catch (e: Exception) {
            logger.warn("Request failed: [${action.path}, echo=$echo] ${e.message}")
            if (e is ActionFailedException) e.json
            else JsonsObject(JsonObject().apply {
                addProperty("status", "failed")
                addProperty("retcode", -1)
                addProperty("message", e.message ?: "")
            })
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
    private fun generateReqJson(action: ActionPath, params: JsonObject?): JsonObject {
        val json = JsonObject()
        json.addProperty("action", action.path)
        if (params != null) json.add("params", params)
        json.addProperty("echo", echo++)
        return json
    }

    companion object {
        val timeout by lazy {
            System.getProperty("overflow.timeout")?.toLongOrNull() ?: 10000L
        }
    }
}
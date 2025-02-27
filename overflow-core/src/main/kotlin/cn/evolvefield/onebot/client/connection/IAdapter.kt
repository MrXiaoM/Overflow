package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.util.ActionSendRequest
import cn.evolvefield.onebot.client.util.OnebotException
import cn.evolvefield.onebot.sdk.util.ignorable
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger

interface IAdapter {
    val scope: CoroutineScope
    val actionHandler: ActionHandler
    val logger: Logger

    fun onReceiveMessage(message: String) {
        try {
            val json = JsonParser.parseString(message).asJsonObject
            if (json.ignorable(META_EVENT, "") != HEART_BEAT) { // 过滤心跳
                logger.debug("[Recv] <-- {}", json.toString())

                if (json.has(API_RESULT_KEY)) { // 接口回调
                    actionHandler.onReceiveActionResp(json)
                } else scope.launch { // 处理事件
                    mutex.withLock {
                        withTimeoutOrNull(processTimeout) {
                            runCatching {
                                EventBus.onReceive(message)
                            }.onFailure {
                                logger.error("处理 Onebot 事件时出现异常: ", it)
                            }
                        } ?: throw IllegalStateException("事件处理超时: $message")
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            logger.error("Json语法错误: {}", message)
        } catch (e: OnebotException) {
            logger.error("解析异常: {}", e.info)
        }
    }

    fun unlockMutex() {
        runCatching {
            if (mutex.isLocked) mutex.unlock()
            if (ActionSendRequest.mutex.isLocked) ActionSendRequest.mutex.unlock()
        }
    }

    companion object {
        private const val META_EVENT = "meta_event_type"
        private const val API_RESULT_KEY = "echo"
        private const val HEART_BEAT = "heartbeat"

        val processTimeout by lazy {
            System.getProperty("overflow.timeout-process")?.toLongOrNull() ?: 20000L
        }
        val mutex = Mutex()
    }
}

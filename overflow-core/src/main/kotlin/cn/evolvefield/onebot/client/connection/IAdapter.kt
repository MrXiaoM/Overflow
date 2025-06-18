package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.handler.EventHolder
import cn.evolvefield.onebot.client.util.ActionSendRequest
import cn.evolvefield.onebot.client.util.OnebotException
import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.sdk.util.nullableInt
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.Bot
import org.slf4j.Logger

internal interface IAdapter {
    val scope: CoroutineScope
    val actionHandler: ActionHandler
    val logger: Logger
    val eventsHolder: MutableMap<Long, MutableList<EventHolder>>

    fun onReceiveMessage(message: String) {
        try {
            val json = JsonParser.parseString(message).asJsonObject
            if (json.ignorable(META_EVENT, "") != HEART_BEAT) { // 过滤心跳
                val echo = json.nullableInt("echo", null)
                if (echo != null) {
                    logger.debug("[Recv][$echo] <-- {}", json.toString())
                } else {
                    logger.debug("[Recv] <-- {}", json.toString())
                }

                if (json.has(API_RESULT_KEY)) { // 接口回调
                    actionHandler.onReceiveActionResp(json)
                } else {
                    val holder = EventBus.matchHandlers(message)
                    val botId = holder.event.selfId
                    if (Bot.findInstance(botId) == null) {
                        val list = eventsHolder[botId] ?: run {
                            mutableListOf<EventHolder>().also {
                                eventsHolder[botId] = it
                            }
                        }
                        list.add(holder)
                        return
                    } else scope.launch { // 处理事件
                        handleEvent(holder)
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            logger.error("Json语法错误: {}", message)
        } catch (e: OnebotException) {
            logger.error("解析异常: {}", e.info)
        }
    }

    fun afterLoginInfoFetch(bot: cn.evolvefield.onebot.client.core.Bot) {
        val events = eventsHolder.remove(bot.id) ?: return
        if (bot.config.dropEventsBeforeConnected) return
        for (holder in events) scope.launch {
            handleEvent(holder)
        }
    }

    private suspend fun handleEvent(holder: EventHolder) { // 处理事件
        mutex.withLock {
            withTimeoutOrNull(processTimeout) {
                runCatching {
                    holder.onReceive()
                }.onFailure {
                    logger.error("处理 Onebot 事件时出现异常: ", it)
                }
            } ?: throw IllegalStateException("事件处理超时: ${holder.event.json}")
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

package cn.evolvefield.onebot.client.handler

import cn.evole.onebot.sdk.event.Event
import cn.evole.onebot.sdk.event.UnsolvedEvent
import cn.evole.onebot.sdk.util.JsonHelper.gson
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.client.listener.message
import cn.evolvefield.onebot.client.util.ListenerUtils
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Project: onebot-client
 * Author: cnlimiter
 * Date: 2023/3/19 15:45
 * Description:
 */
@Suppress("unused")
object EventBus {
    private val log = LoggerFactory.getLogger(EventBus::class.java)
    //存储监听器对象
    private val listeners: MutableList<EventListener<out Event>> = ArrayList()
    //缓存类型与监听器的关系
    private val cache: MutableMap<Class<out Event>, List<EventListener<out Event>>> = ConcurrentHashMap()

    fun addListener(listener: EventListener<out Event>) {
        listeners.add(listener)
    }

    fun stop() {
        cache.clear()
        listeners.clear()
    }

    /**
     * 执行任务
     */
    suspend fun onReceive(message: String) {
        val messageType = ListenerUtils[message] // 获取消息对应的实体类型
        val executes: List<EventListener<out Event>>
        val bean: Event
        if (messageType == null) {
            bean = UnsolvedEvent().also { it.jsonString = message }
            executes = getExecutes(UnsolvedEvent::class.java)
            val json = JsonParser.parseString(message).asJsonObject
            bean.postType = json["post_type"].asString
            bean.time = json["time"].asLong
            bean.selfId = json["self_id"].asLong
        } else {
            bean = gson.fromJson(message, messageType) // 将消息反序列化为对象
            log.debug(String.format("接收到上报消息内容：%s", bean.toString()))
            executes = getExecutes(messageType)
        }
        for (eventListener in executes) {
            eventListener.message(bean) //调用监听方法
        }
    }

    private fun getExecutes(type: Class<out Event>): List<EventListener<out Event>> {
        val list = cache[type]
        return if (list.isNullOrEmpty()) {
            getMethod(type).also {
                cache[type] = it
            }
        } else list
    }

    /**
     * 获取能处理消息类型的处理器
     *
     * @param messageType
     * @return
     */
    private fun getMethod(messageType: Class<out Event>): List<EventListener<out Event>> {
        val eventListeners: MutableList<EventListener<out Event>> = ArrayList()
        for (eventListener in listeners) {
            try {
                if (eventListener.javaClass.declaredMethods.none {
                        it.name == "onMessage" && it.parameterTypes.any { par -> messageType == par }
                    }) continue  //不支持则跳过

                eventListeners.add(eventListener) //开启后添加入当前类型的插件
            } catch (e: Exception) {
                log.error(e.localizedMessage)
            }
        }
        return eventListeners
    }

    val listenerList: List<EventListener<out Event>>
        get() = listeners

    /**
     * 清除类型缓存
     */
    fun cleanCache() {
        cache.clear()
    }
}

package cn.evolvefield.onebot.client.handler

import cn.evole.onebot.sdk.event.Event
import cn.evole.onebot.sdk.util.json.GsonUtil
import cn.evolvefield.onebot.client.listener.EnableEventListener
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.client.listener.message
import cn.evolvefield.onebot.client.util.ListenerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap

/**
 * Project: onebot-client
 * Author: cnlimiter
 * Date: 2023/3/19 15:45
 * Description:
 */
@Suppress("unused")
class EventBus private constructor(protected var channel: Channel<String>) {

    //存储监听器对象
    protected var eventlistenerlist: MutableList<EventListener<out Event>> = ArrayList()

    //缓存类型与监听器的关系
    protected var cache: MutableMap<Class<out Event>, List<EventListener<out Event>>> = ConcurrentHashMap()

    private var close = false

    fun addListener(EventListener: EventListener<out Event>) {
        eventlistenerlist.add(EventListener)
    }

    fun stop() {
        close = true
        cache.clear()
        eventlistenerlist.clear()
    }

    /**
     * 执行任务
     */
    protected suspend fun onReceive(message: String) {
        val messageType = ListenerUtils.getMessageType(message) //获取消息对应的实体类型
        log.debug(String.format("接收到上报消息内容：%s", messageType))
        val bean = GsonUtil.strToJavaBean(message, messageType)!! //将消息反序列化为对象
        val executes = cache[messageType] ?: getMethod(messageType).also {
            cache[messageType] = it
        }
        for (eventListener in executes) {
            eventListener.message(bean) //调用监听方法
        }
    }

    /**
     * 获取能处理消息类型的处理器
     *
     * @param messageType
     * @return
     */
    protected fun getMethod(messageType: Class<out Event>): List<EventListener<out Event>> {
        val eventListeners: MutableList<EventListener<out Event>> = ArrayList()
        for (eventListener in eventlistenerlist) {
            try {
                try {
                    eventListener.javaClass.getMethod("onMessage", messageType) //判断是否注册监听器
                } catch (e: NoSuchMethodException) {
                    continue  //不支持则跳过
                }
                if (eventListener is EnableEventListener<*>) {
                    if (!eventListener.enable()) { //检测是否开启该插件
                        continue
                    }
                }
                eventListeners.add(eventListener) //开启后添加入当前类型的插件
            } catch (e: Exception) {
                log.error(e.localizedMessage)
            }
        }
        return eventListeners
    }

    val listenerList: List<EventListener<out Event>>
        get() = eventlistenerlist

    /**
     * 清除类型缓存
     */
    fun cleanCache() {
        cache.clear()
    }

    companion object {
        private val log = LoggerFactory.getLogger(EventBus::class.java)
        fun create(scope: CoroutineScope, channel: Channel<String>): EventBus {
            return EventBus(channel).apply {
                scope.launch {
                    select {
                        channel.onReceive { onReceive(it) }
                    }
                }
            }
        }
    }
}
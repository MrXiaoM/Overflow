package cn.evolvefield.onebot.client.handler

import cn.evole.onebot.sdk.event.Event
import cn.evole.onebot.sdk.util.json.GsonUtil
import cn.evolvefield.onebot.client.listener.EnableEventListener
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.client.listener.message
import cn.evolvefield.onebot.client.util.ListenerUtils
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
open class EventBus(protected var queue: BlockingQueue<String>) : Runnable {
    //存储监听器对象
    protected var eventlistenerlist: MutableList<EventListener<out Event>> = ArrayList()

    //缓存类型与监听器的关系
    protected var cache: MutableMap<Class<out Event>, List<EventListener<out Event>>> = ConcurrentHashMap()

    //线程池 用于并发执行队列中的任务
    protected var service: Thread
    private var close = false

    init {
        service = Thread(this)
        service.start()
    }

    fun addListener(EventListener: EventListener<out Event>) {
        eventlistenerlist.add(EventListener)
    }

    fun stop() {
        close = true
        cache.clear()
        eventlistenerlist.clear()
        service.interrupt()
    }

    override fun run() {
        try {
            while (!close) {
                runTask()
            }
        } catch (e: Exception) {
            log.error(e.message)
        }
    }

    /**
     * 执行任务
     */
    protected fun runTask() {
        val message = task //获取消息
        if (message == "null") {
            log.debug("消息队列为空")
            return
        }
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

    protected val task: String
        /**
         * 从队列中获取任务
         *
         * @return
         */
        get() {
            try {
                return queue.take()
            } catch (e: Exception) {
                log.error(e.message)
            }
            return "null"
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
    }
}
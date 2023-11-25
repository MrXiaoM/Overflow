package cn.evolvefield.onebot.client.handler

import cn.evole.onebot.sdk.event.Event
import cn.evolvefield.onebot.client.listener.SimpleEventListener

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 16:14
 * Version: 1.0
 */
abstract class DefaultEventHandler<T : Event> : SimpleEventListener<T>() {
    private var handlerMap: MutableMap<String, Handler<T>> = HashMap()

    fun addHandler(key: String, handler: Handler<T>) {
        handlerMap[key] = handler
    }

    fun removeHandler(key: String) {
        handlerMap.remove(key)
    }

    fun getHandler(key: String): Handler<T> {
        return handlerMap[key]!!
    }

    operator fun contains(key: String): Boolean {
        return handlerMap.containsKey(key)
    }
}
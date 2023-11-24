package cn.evolvefield.onebot.client.listener

import cn.evole.onebot.sdk.event.Event

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 16:11
 * Version: 1.0
 */
interface EventListener<T : Event> {
    /**
     * 监听到消息
     *
     * @param t 消息实体
     */
    fun onMessage(t: T)
}
@Suppress("unchecked_cast")
internal fun <T : Event> EventListener<T>.message(event: Event) {
    onMessage(event as T)
}

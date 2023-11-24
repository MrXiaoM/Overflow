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
     * @param e 消息实体
     */
    suspend fun onMessage(e: T)
}
@Suppress("unchecked_cast")
internal suspend fun <T : Event> EventListener<T>.message(event: Event) {
    onMessage(event as T)
}

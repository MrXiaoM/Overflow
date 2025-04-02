package cn.evolvefield.onebot.client.handler

import cn.evolvefield.onebot.sdk.event.Event

internal class EventHolder(
    val event: Event,
    val handlers: List<Handler<*>>,
) {
    suspend fun onReceive() {
        for (handler in handlers) {
            handler.onReceive(event)
        }
    }
}

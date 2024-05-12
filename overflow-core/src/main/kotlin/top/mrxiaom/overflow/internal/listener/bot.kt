package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.sdk.event.UnsolvedEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.broadcast
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.OverflowAPI.Companion.logger
import top.mrxiaom.overflow.event.UnsolvedOnebotEvent
import top.mrxiaom.overflow.internal.scope

internal fun addBotListeners() {
    listOf(
        UnsolvedEventListener(),

    ).forEach(EventBus::addListener)
}

internal class UnsolvedEventListener : EventListener<UnsolvedEvent> {
    override suspend fun onMessage(e: UnsolvedEvent) {
        Overflow.scope.launch {
            UnsolvedOnebotEvent(e.selfId, e.jsonString, e.time).broadcast()
        }
    }
}

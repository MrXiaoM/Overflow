package cn.evolvefield.onebot.client.handler

import cn.evolvefield.onebot.sdk.event.Event
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.utils.bot
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

internal class Handler<T : Event>(
    val type: KClass<T>,
    private val condition: ((T) -> Boolean)?,
    private val block: suspend BotWrapper.(T) -> Unit
) {
    suspend fun onReceive(event: Event) {
        val e = type.safeCast(event)
        if (e != null) {
            val bot = e.bot ?: return
            if (condition?.invoke(e) != false) {
                block.invoke(bot, e)
            }
        }
    }
}

package cn.evolvefield.onebot.client.handler

import cn.evolvefield.onebot.sdk.event.Event
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.utils.bot
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

internal abstract class Handler<T : Event>(
    val type: KClass<T>,
) {
    suspend fun onReceive(event: Event) {
        val e = type.safeCast(event)
        if (e != null) {
            receive(e)
        }
    }

    abstract suspend fun receive(e: T)
}
internal class BotHandler<T : Event>(
    type: KClass<T>,
    private val condition: ((T) -> Boolean)?,
    private val block: suspend BotWrapper.(T) -> Unit
): Handler<T>(type) {
    override suspend fun receive(e: T) {
        val bot = e.bot ?: return
        if (condition?.invoke(e) != false) {
            block.invoke(bot, e)
        }
    }
}
internal class NormalHandler<T : Event>(
    type: KClass<T>,
    private val condition: ((T) -> Boolean)?,
    private val block: suspend (T) -> Unit
): Handler<T>(type) {
    override suspend fun receive(e: T) {
        if (condition?.invoke(e) != false) {
            block.invoke(e)
        }
    }
}

package top.mrxiaom.overflow.internal.utils

import java.util.concurrent.atomic.AtomicLong

internal class RequestManager {
    val ids = AtomicLong(0L)
    val map = mutableMapOf<Long, String>()

    fun put(eventFlag: String): Long {
        val id = ids.getAndIncrement()
        map[id] = eventFlag
        return id
    }

    fun get(id: Long): String? {
        return map[id]
    }
}

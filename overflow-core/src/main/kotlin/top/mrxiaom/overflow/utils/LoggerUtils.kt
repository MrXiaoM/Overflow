package top.mrxiaom.overflow.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger


@Suppress("INVISIBLE_MEMBER")
internal fun MiraiLogger.subLogger(name: String): MiraiLogger {
    return net.mamoe.mirai.internal.utils.subLoggerImpl(this, name)
}
internal fun MiraiLogger.asCoroutineExceptionHandler(
    priority: SimpleLogger.LogPriority = SimpleLogger.LogPriority.ERROR,
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { context, e ->
        call(
            priority,
            context[CoroutineName]?.let { "Exception in coroutine '${it.name}'." } ?: "Exception in unnamed coroutine.",
            e
        )
    }
}
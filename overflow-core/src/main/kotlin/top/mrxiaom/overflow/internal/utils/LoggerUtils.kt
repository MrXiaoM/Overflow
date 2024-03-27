package top.mrxiaom.overflow.internal.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.utils.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass


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

internal fun getCurrentDay() = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
internal fun getCurrentDate() = SimpleDateFormat("yyyy-MM-dd").format(Date())

private val doNothing: (priority: SimpleLogger.LogPriority, message: String?, e: Throwable?) -> Unit =
    { _: SimpleLogger.LogPriority, _: String?, _: Throwable? -> error("stub") }

/**
 * 将日志写入('append')到特定文件夹中的文件. 每日日志独立保存.
 *
 * @see PlatformLogger 查看格式信息
 */
internal class LoggerInFolder @JvmOverloads constructor(
    requester: KClass<*>,
    identity: String,
    private val directory: File = File(identity),
    /**
     * 保留日志文件多长时间. 毫秒数
     */
    private val retain: Long = 1.weeksToMillis
) : SimpleLogger("", doNothing) {
    private val inner = MiraiLogger.Factory.create(requester, identity)
    init {
        directory.mkdirs()
    }

    private fun checkOutdated() {
        val current = currentTimeMillis()
        directory.walk().filter(File::isFile).filter { current - it.lastModified() > retain }.forEach {
            it.delete()
        }
    }

    private var day = getCurrentDay()

    private var delegate: WithFileLogger = WithFileLogger(inner, File(directory, "${getCurrentDate()}.log"))
        get() {
            val currentDay = getCurrentDay()
            if (day != currentDay) {
                day = currentDay
                checkOutdated()
                field = WithFileLogger(inner, File(directory, "${getCurrentDate()}.log"))
            }
            return field
        }

    override val logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit =
        { priority: LogPriority, message: String?, e: Throwable? ->
            delegate.call(priority, message, e)
        }
}

/**
 * 将日志写入('append')到特定文件.
 *
 * @see PlatformLogger 查看格式信息
 */
@OptIn(MiraiInternalApi::class)
internal class WithFileLogger(
    val logger: MiraiLogger,
    file: File
) : MiraiLogger, PlatformLogger(logger.identity, {
    synchronized(logger) {
        file.appendText(it + "\n")
    }
}, false) {
    // Implementation notes v2.5.0:
    // Extending `PlatformLogger` for binary compatibility for JVM target only.
    // See actual declaration in androidMain for a better impl (implements `MiraiLogger` only)

    public constructor(logger: MiraiLogger) : this(logger, File("${logger.identity}-${getCurrentDate()}.log"))

    init {
        file.createNewFile()
        require(file.isFile) { "Log file must be a file: $file" }
        require(file.canWrite()) { "Log file must be write: $file" }
    }

    override fun debug0(message: String?) {
        logger.debug(message)
        super.debug0(message)
    }

    override fun error0(message: String?) {
        logger.error(message)
        super.error0(message)
    }

    override fun info0(message: String?) {
        logger.info(message)
        super.info0(message)
    }

    override fun verbose0(message: String?) {
        logger.verbose(message)
        super.verbose0(message)
    }

    override fun warning0(message: String?) {
        logger.warning(message)
        super.warning0(message)
    }
}

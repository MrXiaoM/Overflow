package top.mrxiaom.overflow.message

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.Message
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * 消息处理器，用于扩展消息类型
 */
interface MessageProcessor<T : Message> {
    /**
     * 消息段中的 type 字段，消息类型名称
     */
    val type: String

    /**
     * 将 mirai 消息转换为 onebot 消息
     *
     * @throws IllegalArgumentException 抛出此异常，将会清空 JsonObjectBuilder 中键入的数据，并尝试寻找下一个可用的消息处理器
     */
    fun miraiToOnebot(message: T, block: JsonObjectBuilder)

    /**
     * 将 onebot 消息转换为 mirai 消息
     *
     * 有 [ForOnebot] 注解的消息处理器优先进行解析，
     *
     * @throws IllegalArgumentException 抛出此异常，将会尝试寻找下一个可用的消息处理器
     */
    suspend fun onebotToMirai(bot: Bot, data: JsonObject): T
}

annotation class MiraiTypes(
    vararg val value: KClass<*>
)

annotation class ForOnebot(
    vararg val value: String
)

/**
 * 该消息类型适用于什么 mirai 消息类型
 */
fun <T : MessageProcessor<*>> T.miraiTypes(): List<KClass<*>> {
    val types = this::class.findAnnotation<MiraiTypes>()
    return types?.value?.toList() ?: listOf()
}

/**
 * 该消息类型适用于什么 Onebot 实现
 *
 * 如果为空，则可用于所有 Onebot 实现
 */
fun <T : MessageProcessor<*>> T.appName(): List<String> {
    val types = this::class.findAnnotation<ForOnebot>()
    return types?.value?.toList() ?: listOf()
}

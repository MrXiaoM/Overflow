@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package top.mrxiaom.overflow.spi.message

import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.spi.BaseService
import net.mamoe.mirai.spi.SpiServiceLoader
import top.mrxiaom.overflow.message.BuildMessageContext

/**
 * 消息序列化器接口
 */
public interface JsonMessageSerializer : BaseService {
    override val priority: Int
        get() = 1000
    /**
     * Json 消息的类型是否匹配
     */
    public fun isMatchJson(element: JsonObject): Boolean

    /**
     * Mirai 消息的类型是否匹配
     */
    public fun isMatchMirai(message: Message): Boolean

    /**
     * 将 Json 消息转换为 Mirai 消息
     * @param context 消息构建上下文，最终将元素添加到这里
     * @param element 待转换的 Json 消息
     * @return 是否转换成功？如果转换失败，将会交给下一个序列化器进行处理
     */
    public suspend fun toMirai(context: BuildMessageContext.ToMirai, element: JsonObject): Boolean

    /**
     * 将 Mirai 消息转换为 Json 消息
     * @param context 消息构建上下文，最终将元素添加到这里
     * @param message 待转换的 Mirai 消息
     * @return 是否转换成功？如果转换失败，将会交给下一个序列化器进行处理
     */
    public fun toJson(context: BuildMessageContext.ToJson, message: Message): Boolean

    companion object {
        private val loader = SpiServiceLoader(JsonMessageSerializer::class)

        val instances: List<JsonMessageSerializer>
            get() = loader.allServices.sortedBy { it.priority }
    }
}

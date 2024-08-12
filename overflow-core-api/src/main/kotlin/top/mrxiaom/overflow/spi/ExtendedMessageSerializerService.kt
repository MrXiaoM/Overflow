@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.spi

import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.spi.BaseService
import net.mamoe.mirai.spi.SpiServiceLoader
import top.mrxiaom.overflow.contact.RemoteBot

/**
 * 额外的消息序列化器，用于处理不同 Onebot 平台的特殊情况
 */
interface ExtendedMessageSerializerService : BaseService {

    /**
     * 反序列化 Onebot 消息段为 Mirai 消息
     * CQ 码会先转为消息段再进行处理，无需担心CQ码兼容问题
     *
     * @param bot 机器人实例
     * @param type 消息类型
     * @param data 消息数据
     * @return Mirai 消息，返回 null 代表不进行额外处理，交给其它序列化器或 Overflow 自带序列化器处理
     */
    suspend fun deserialize(bot: RemoteBot, type: String, data: JsonObject): Message?

    /**
     * 序列化 Mirai 消息为 Onebot 消息段
     *
     * @param bot 机器人实例
     * @param message Mirai 消息
     * @return Onebot 消息段的类型与数据，返回 null 代表不进行额外处理，交给其它序列化器或 Overflow 自带序列化器处理
     */
    fun serialize(bot: RemoteBot?, message: Message): Pair<String, JsonObject>?

    companion object {
        private val loader = SpiServiceLoader(ExtendedMessageSerializerService::class)

        internal val instances: List<ExtendedMessageSerializerService>
            get() = loader.allServices.sortedBy { it.priority }

        suspend fun List<ExtendedMessageSerializerService>.deserialize(
            bot: RemoteBot,
            type: String,
            data: JsonObject
        ): Message? {
            for (ext in this) {
                val message = ext.deserialize(bot, type, data) ?: continue
                return message
            }
            return null
        }

        fun List<ExtendedMessageSerializerService>.serialize(
            bot: RemoteBot?,
            message: Message
        ): Pair<String, JsonObject>? {
            for (ext in this) {
                val pair = ext.serialize(bot, message) ?: continue
                return pair
            }
            return null
        }
    }
}

package top.mrxiaom.overflow.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import top.mrxiaom.overflow.contact.RemoteBot

/**
 * 消息构建上下文
 */
public interface BuildMessageContext<E, R> {
    /**
     * 机器人实例
     */
    val bot: RemoteBot?

    val appName: String
        get() = bot?.appName ?: "Onebot"

    val appVersion: String
        get() = bot?.appVersion ?: "Unknown"

    /**
     * 消息构建时产生的临时额外数据
     */
    val extra: MutableMap<String, Any>

    /**
     * 获取当前正在处理的消息元素索引
     */
    val currentIndex: Int

    fun add(element: E)

    fun build(): R

    public interface ToMirai : BuildMessageContext<Message, MessageChain> {
        override val bot: RemoteBot
        /**
         * 消息来源
         */
        val source: MessageSource?

    }

    public interface ToJson : BuildMessageContext<JsonObject, JsonArray> {
        fun add(type: String, block: JsonObjectBuilder.() -> Unit) {
            add(type, buildJsonObject(block))
        }
        fun add(type: String, data: JsonObject) {
            add(buildJsonObject {
                put("type", type)
                put("data", data)
            })
        }
    }
}

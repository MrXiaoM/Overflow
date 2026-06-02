package top.mrxiaom.overflow.internal.message

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.MessageSource
import top.mrxiaom.overflow.OverflowAPI.Companion.logger
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.data.UnknownMessage
import top.mrxiaom.overflow.message.BuildMessageContext
import top.mrxiaom.overflow.spi.ExtendedMessageSerializerService
import top.mrxiaom.overflow.spi.ExtendedMessageSerializerService.Companion.deserialize
import top.mrxiaom.overflow.spi.ExtendedMessageSerializerService.Companion.serialize
import top.mrxiaom.overflow.spi.message.JsonMessageSerializer

internal interface BuildMessageContextImpl<E, R> : BuildMessageContext<E, R> {

    class ToMirai(
        override val bot: RemoteBot,
        override val source: MessageSource?,
        resolveReplyMessage: Boolean = true
    ) : BuildMessageContextImpl<Message, MessageChain>, BuildMessageContext.ToMirai {
        override val extra: MutableMap<String, Any> = mutableMapOf()
        override var currentIndex: Int = 0
        private val builder = MessageChainBuilder()
        init {
            if (resolveReplyMessage) {
                extra["resolveReplyMessage"] = true
            }
            if (source != null) {
                add(source)
            }
        }

        suspend fun resolve(input: JsonArray) {
            val serializers = JsonMessageSerializer.instances
            val extensions = ExtendedMessageSerializerService.instances
            for ((i, o) in input.withIndex()) {
                currentIndex = i
                val obj = o.jsonObject
                val type = obj["type"].string
                val data = obj["data"]?.jsonObject ?: buildJsonObject {}
                val extMessage = extensions.deserialize(bot, type, data)
                if (extMessage != null) {
                    add(extMessage)
                    continue
                }
                try {
                    var exists = false
                    for (serializer in serializers) {
                        if (serializer.isMatchJson(obj) && serializer.toMirai(this, obj)) {
                            exists = true
                            break
                        }
                    }
                    if (!exists) {
                        add(UnknownMessage(type, Json.encodeToString(data)).printLog())
                    }
                } catch (t: Throwable) {
                    logger.warning("解析消息 $type -> $data 时出现错误 (${appName} v${appVersion})", t)
                }
            }
        }

        override fun add(element: Message) {
            builder.add(element)
        }

        override fun build(): MessageChain {
            return builder.build()
        }
    }

    class ToJson(
        override val bot: RemoteBot?,
    ) : BuildMessageContextImpl<JsonObject, JsonArray>, BuildMessageContext.ToJson {
        override val extra: MutableMap<String, Any> = mutableMapOf()
        override var currentIndex: Int = 0
        private val builder = mutableListOf<JsonObject>()

        fun resolve(message: Message) {
            val serializers = JsonMessageSerializer.instances
            val extensions = ExtendedMessageSerializerService.instances
            val messageChain = (message as? MessageChain) ?: listOf(message)
            for ((i, o) in messageChain.withIndex()) {
                currentIndex = i
                val pair = extensions.serialize(bot, message)
                if (pair != null) {
                    add(pair.first, pair.second)
                    continue
                }
                var exists = false
                for (serializer in serializers) {
                    if (serializer.isMatchMirai(o) && serializer.toJson(this, o)) {
                        exists = true
                        break
                    }
                }
                if (!exists) {
                    logger.warning("消息元素 $message 没有合适的序列化器，无法转换为 JSON 格式")
                }
            }
        }

        override fun add(element: JsonObject) {
            builder.add(element)
        }

        override fun build(): JsonArray {
            return buildJsonArray {
                for (obj in builder) {
                    add(obj)
                }
            }
        }
    }

    companion object {

        internal suspend fun toMirai(
            bot: RemoteBot,
            source: MessageSource?,
            json: JsonArray,
            resolveReplyMessage: Boolean = true
        ): MessageChain {
            val context = ToMirai(bot, source, resolveReplyMessage)
            context.resolve(json)
            return context.build()
        }

        internal fun toJson(
            bot: RemoteBot?,
            message: Message
        ): JsonArray {
            val context = ToJson(bot)
            context.resolve(message)
            return context.build()
        }

    }
}

package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class PlainTextSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "text"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is PlainText
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val content = data["text"].string
        if (content.isNotEmpty()) {
            context.add(PlainText(content))
            return true
        }
        return false
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? PlainText ?: return false
        context.add("text") {
            put("text", element.content)
        }
        return true
    }
}
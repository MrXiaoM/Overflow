package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.ServiceMessage
import net.mamoe.mirai.message.data.SimpleServiceMessage
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class ServiceMessageSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "xml"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is ServiceMessage
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        context.add(SimpleServiceMessage(60, data["data"].string))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? ServiceMessage ?: return false
        context.add("xml") {
            put("data", element.content)
        }
        return true
    }
}
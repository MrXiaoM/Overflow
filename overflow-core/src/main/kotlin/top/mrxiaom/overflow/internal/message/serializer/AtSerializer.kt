package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class AtSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "at"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is At || message is AtAll
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        if (context.currentIndex == 1 && context.extra.containsKey("hasQuote"))
            return true
        if (data["qq"].string.lowercase() == "all") {
            context.add(AtAll)
        } else {
            context.add(At(data["qq"].string.toLong()))
        }
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? At
        if (element != null) {
            context.add("at") {
                put("qq", element.target.toString())
            }
            return true
        }
        if (message is AtAll) {
            context.add("at") {
                put("qq", "all")
            }
            return true
        }
        return false
    }
}
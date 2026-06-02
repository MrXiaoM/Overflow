package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PokeMessage
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class PokeSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "poke"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is PokeMessage
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        context.add(PokeMessage(
            data["name"].string,
            data["type"].string.toInt(),
            data["id"].string.toInt()
        ))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? PokeMessage ?: return false
        context.add("poke") {
            put("type", element.pokeType.toString())
            put("id", element.id.toString())
        }
        return true
    }
}
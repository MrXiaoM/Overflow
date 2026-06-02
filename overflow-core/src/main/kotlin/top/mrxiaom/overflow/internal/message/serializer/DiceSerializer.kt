package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.int
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class DiceSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        val type = element["type"].string
        return type == "dice" || type == "new_dice"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is Dice
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val type = element["type"].string
        val data = element.data() ?: return false
        when (type) {
            "dice" -> {
                // result: LLOnebot, NapCat
                val id = (data["result"] ?: data["id"])?.string?.toInt()
                if (id != null && id in 1..6) {
                    context.add(Dice(id))
                } else {
                    context.add(Dice.random()) // not support
                }
            }

            "new_dice" -> context.add(Dice(data["id"].int))
            else -> return false
        }
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? Dice ?: return false
        val type = when (context.appName.lowercase()) {
            "shamrock" -> "new_dice"
            else -> "dice"
        }
        context.add(type) {
            put("id", element.value)
            put("result", element.value) // LLOnebot, NapCat
        }
        return true
    }
}

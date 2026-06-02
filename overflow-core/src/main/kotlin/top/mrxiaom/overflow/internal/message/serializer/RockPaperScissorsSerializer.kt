package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.RockPaperScissors
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class RockPaperScissorsSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "rps"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is RockPaperScissors
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val id = (data["result"] ?: data["id"])?.string?.toInt()
        when (id) {
            1 -> context.add(RockPaperScissors.PAPER)
            2 -> context.add(RockPaperScissors.SCISSORS)
            3 -> context.add(RockPaperScissors.ROCK)
            else -> context.add(RockPaperScissors.random()) // not support
        }
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? RockPaperScissors ?: return false
        context.add("rps") {
            val id = when (element) {
                RockPaperScissors.PAPER -> 1
                RockPaperScissors.SCISSORS -> 2
                RockPaperScissors.ROCK -> 3
            }
            put("id", id)
            put("result", id) // LLOnebot, NapCat
        }
        return true
    }
}
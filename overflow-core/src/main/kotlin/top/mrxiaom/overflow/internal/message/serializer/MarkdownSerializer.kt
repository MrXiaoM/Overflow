package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.data.Markdown
import top.mrxiaom.overflow.message.BuildMessageContext

class MarkdownSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "markdown"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is Markdown
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        when (context.appName.lowercase()) {
            // 其它实现可能有其它格式，预留判断
            "shamrock" -> context.add(Markdown(data["content"].string))
            else -> context.add(Markdown(data["content"].string))
        }
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? Markdown ?: return false
        context.add("markdown") {
            when (context.appName.lowercase()) {
                // 其它实现可能有其它格式，预留判断
                "shamrock" -> put("content", element.content)
                else -> put("content", element.content)
            }
        }
        return true
    }
}
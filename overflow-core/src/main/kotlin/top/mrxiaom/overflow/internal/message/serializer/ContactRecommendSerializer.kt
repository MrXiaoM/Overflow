package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.data.ContactRecommend
import top.mrxiaom.overflow.message.BuildMessageContext

class ContactRecommendSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "contact"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is ContactRecommend
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val contactType = when (val typeStr = data["type"].string.lowercase()) {
            "group" -> ContactRecommend.ContactType.Group
            "private", "qq" -> ContactRecommend.ContactType.Private
            else -> throw IllegalArgumentException("未知联系人类型 $typeStr")
        }
        val id = data["id"].string
            .substringBefore("&") // OpenShamrock bug
            .toLong()
        context.add(ContactRecommend(contactType, id))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? ContactRecommend ?: return false
        context.add("contact") {
            put("type", element.contactType.name.lowercase())
            put("id", element.id.toString())
        }
        return true
    }
}
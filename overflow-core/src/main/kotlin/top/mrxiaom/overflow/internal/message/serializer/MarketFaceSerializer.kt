package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class MarketFaceSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "mface"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is MarketFaceImpl
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val key = data["key"]?.string ?: ""
        val emojiId = (data["emoji_id"] ?: data["id"])?.string ?: ""
        val emojiPackageId = data["emoji_package_id"]?.string ?: ""
        val summary = data["summary"]?.string ?: "[动画表情]"
        context.add(
            MarketFaceImpl(
                ImMsgBody.MarketFace(
                    key = key.encodeToByteArray(),
                    faceId = emojiId.encodeToByteArray(),
                    param = emojiPackageId.encodeToByteArray(),
                    faceName = summary.encodeToByteArray(),
                )
            )
        )
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? MarketFaceImpl ?: return false
        context.add("mface") {
            val key = element.delegate.key.decodeToString()
            val emojiId = element.delegate.faceId.decodeToString()
            val emojiPackageId = element.delegate.param.decodeToString()
            val summary = element.delegate.faceName.decodeToString()
            // LLOnebot: 发送商城表情
            if (key.isNotBlank() && emojiId.isNotBlank() && emojiPackageId.isNotBlank()) {
                put("key", key)
                put("id", emojiId)
                put("emoji_id", emojiId)
                put("emoji_package_id", emojiPackageId)
                put("summary", summary)
            }
        }
        return true
    }
}
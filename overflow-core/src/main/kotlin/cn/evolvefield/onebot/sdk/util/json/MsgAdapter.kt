package cn.evolvefield.onebot.sdk.util.json

import cn.evolvefield.onebot.sdk.response.group.GetMsgResp
import cn.evolvefield.onebot.sdk.util.*
import com.google.gson.*
import java.lang.reflect.Type

class MsgAdapter : JsonDeserializerKt<GetMsgResp> {
    override fun deserializeFromJson(
        json: JsonElement,
        type: Type,
        ctx: JsonDeserializationContext
    ): GetMsgResp {
        val obj = json.asJsonObject
        return GetMsgResp().apply {
            messageId = obj.messageId()
            realId = obj.ignorable("real_id", 0)
            sender = obj.fromJson("sender")!!
            time = obj.int("time")
            isJsonMessage = obj.has("message") && obj["message"].isJsonArray
            message = obj.forceString("message")
            rawMessage = obj.ignorable("raw_message", "")
            peerId = obj.ignorable("peer_id", 0L)
            groupId = obj.ignorable("group_id", 0L)
            targetId = obj.ignorable("target_id", 0L)
        }
    }

    companion object {
        fun JsonObject.messageId(): Int {
            val msgId: String = this["message_id"]?.asString ?: throw IllegalStateException("""
                收到消息ID为空的消息事件。type=${this["message_type"]}, sub_type=${this["sub_type"]}
                请向你所使用的 Onebot 实现维护者报告该问题，
                不要将该问题反馈到 Overflow。
            """.trimIndent()
            )
            try {
                return msgId.toInt()
            } catch (ignored: NumberFormatException) {
                throw NumberFormatException(
                    """
                        无法将消息ID $msgId 的类型转换为 int32，
                        请向你所使用的 Onebot 实现维护者报告该问题，
                        不要将该问题反馈到 Overflow。
                    """.trimIndent()
                )
            }
        }
    }
}
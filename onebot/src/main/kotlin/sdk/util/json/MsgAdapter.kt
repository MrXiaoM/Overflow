package cn.evolvefield.onebot.sdk.util.json

import cn.evolvefield.onebot.sdk.response.group.GetMsgResp
import cn.evolvefield.onebot.sdk.util.forceString
import cn.evolvefield.onebot.sdk.util.fromJson
import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.sdk.util.int
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class MsgAdapter : JsonDeserializer<GetMsgResp> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): GetMsgResp {
        val obj = json.asJsonObject
        return GetMsgResp().apply {
            messageId = obj.messageId()
            realId = obj.ignorable("real_id", 0)
            sender = obj.fromJson("sender")!!
            time = obj.int("time")
            message = obj.forceString("message")
            rawMessage = obj.ignorable("raw_message", "")
            peerId = obj.ignorable("peer_id", 0L)
            groupId = obj.ignorable("group_id", 0L)
            targetId = obj.ignorable("target_id", 0L)
        }
    }

    companion object {
        fun JsonObject.messageId(): Int {
            val msgId: String = this["message_id"].asString
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
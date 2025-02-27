package cn.evolvefield.onebot.sdk.util.json

import cn.evolvefield.onebot.sdk.entity.PrivateSender
import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.sdk.event.message.GuildMessageEvent
import cn.evolvefield.onebot.sdk.event.message.MessageEvent
import cn.evolvefield.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.sdk.util.*
import cn.evolvefield.onebot.sdk.util.json.MsgAdapter.Companion.messageId
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class MessageEventAdapter : JsonDeserializerKt<MessageEvent> {
    override fun deserializeFromJson(
        json: JsonElement,
        type: Type,
        ctx: JsonDeserializationContext
    ): MessageEvent? {
        val obj = json.asJsonObject
        val subType = obj.string("sub_type")
        val messageType = obj.string("message_type")
        return when (messageType) {
            "group" -> obj.groupMessage(subType)
            "private" -> obj.privateMessage(subType)
            "guild" -> obj.guildMessage(subType)
            else -> null
        }?.apply {
            postType = obj.string("post_type")
            time = obj.long("time")
            selfId = obj.long("self_id")
            userId = obj.long("user_id")
            isJsonMessage = obj.has("message") && obj["message"].isJsonArray
            message = obj.forceString("message")
            rawMessage = obj.ignorable("raw_message", "")
            font = obj.ignorable("font", 0)
        }
    }

    private fun JsonObject.groupMessage(subType: String): MessageEvent = GroupMessageEvent().apply {
        this.subType = subType
        messageId = messageId()
        groupId = long("group_id")
        anonymous = fromJson("anonymous")
        sender = fromJson("sender")
    }
    private fun JsonObject.privateMessage(subType: String): MessageEvent = PrivateMessageEvent().apply {
        this.subType = subType
        messageId = messageId()
        tempSource = ignorable("temp_source", Int.MIN_VALUE)
        sender = fromJson("sender")!!
        groupId = ignorable("group_id", sender.groupId)
        fromNick = ignorable("from_nick", sender.nickname)
    }
    private fun JsonObject.guildMessage(subType: String): MessageEvent = GuildMessageEvent().apply {
        this.subType = subType
        messageId = string("message_id")
        guildId = string("guild_id")
        channelId = string("channel_id")
        selfTinyId = string("self_tiny_id")
        sender = fromJson("sender")!!
    }

    class PrivateSenderAdapter : JsonDeserializer<PrivateSender> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): PrivateSender {
            val obj = json.asJsonObject
            return PrivateSender().apply {
                userId = obj["user_id"].asLong
                groupId = obj.ignorable("group_id", 0L)
                nickname = obj["nickname"].asString
                sex = obj.ignorable("sex", "unknown")
                age = obj.ignorable("age", 0)
            }
        }
    }
}

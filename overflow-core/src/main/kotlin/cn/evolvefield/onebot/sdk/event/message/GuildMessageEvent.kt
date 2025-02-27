package cn.evolvefield.onebot.sdk.event.message

import cn.evolvefield.onebot.sdk.entity.GuildSender
import cn.evolvefield.onebot.sdk.util.json.MessageEventAdapter
import com.google.gson.annotations.JsonAdapter

@JsonAdapter(MessageEventAdapter::class)
class GuildMessageEvent : MessageEvent() {
    var messageId = ""
    var subType = ""
    var guildId = ""
    var channelId = ""
    var selfTinyId = ""
    lateinit var sender: GuildSender
}

package cn.evolvefield.onebot.sdk.event.message

import cn.evolvefield.onebot.sdk.entity.GuildSender
import cn.evolvefield.onebot.sdk.util.json.MessageEventAdapter
import com.google.gson.annotations.JsonAdapter
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonAdapter(MessageEventAdapter::class)
class GuildMessageEvent : MessageEvent() {
    var messageId = ""
    var subType = ""
    var guildId = ""
    var channelId = ""
    var selfTinyId = ""
    lateinit var sender: GuildSender
}

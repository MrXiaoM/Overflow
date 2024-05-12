package cn.evolvefield.onebot.sdk.response.guild

import cn.evolvefield.onebot.sdk.entity.GuildSender
import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GetGuildMsgResp {
    @SerializedName("guild_id")
    var guildId = ""

    @SerializedName("channel_id")
    var channelId = ""

    @SerializedName("message")
    var message = ""

    @SerializedName("message_id")
    var messageId = ""

    @SerializedName("message_seq")
    var messageSeq = 0

    @SerializedName("message_source")
    var messageSource = ""

    @SerializedName("sender")
    private val sender: GuildSender? = null

    @SerializedName("time")
    var time = 0L
}

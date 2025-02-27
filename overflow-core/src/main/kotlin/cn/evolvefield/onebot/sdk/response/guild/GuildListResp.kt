package cn.evolvefield.onebot.sdk.response.guild

import com.google.gson.annotations.SerializedName

class GuildListResp {
    @SerializedName("guild_id")
    var guildId = ""

    @SerializedName("guild_name")
    var guildName = ""

    @SerializedName("guild_display_id")
    var guildDisplayId = ""
}

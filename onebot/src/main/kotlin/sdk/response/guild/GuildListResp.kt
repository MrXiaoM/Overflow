package cn.evolvefield.onebot.sdk.response.guild

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GuildListResp {
    @SerializedName("guild_id")
    var guildId = ""

    @SerializedName("guild_name")
    var guildName = ""

    @SerializedName("guild_display_id")
    var guildDisplayId = ""
}

package cn.evolvefield.onebot.sdk.response.guild

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GuildMetaByGuestResp {
    @SerializedName("guild_id")
    var guildId = ""

    @SerializedName("guild_name")
    var guildName = ""

    @SerializedName("guild_profile")
    var guildProfile = ""

    @SerializedName("create_time")
    var createTime = 0L

    @SerializedName("max_member_count")
    var maxMemberCount = 0L

    @SerializedName("max_robot_count")
    var maxRobotCount = 0L

    @SerializedName("max_admin_count")
    var maxAdminCount = 0L

    @SerializedName("member_count")
    var memberCount = 0L

    @SerializedName("owner_id")
    var ownerId = ""
}

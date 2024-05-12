package cn.evolvefield.onebot.sdk.response.guild

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GuildMemberProfileResp {
    @SerializedName("tiny_id")
    var tinyId: String = ""

    @SerializedName("nickname")
    var nickname: String = ""

    @SerializedName("avatar_url")
    var avatarUrl: String = ""

    @SerializedName("join_time")
    var joinTime: String = ""

    @SerializedName("roles")
    var roles: List<RoleInfo> = mutableListOf()

    @Data
    class RoleInfo {
        @SerializedName("role_id")
        var roleId = ""

        @SerializedName("role_name")
        var roleName = ""
    }
}

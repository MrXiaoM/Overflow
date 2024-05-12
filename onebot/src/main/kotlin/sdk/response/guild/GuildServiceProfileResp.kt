package cn.evolvefield.onebot.sdk.response.guild

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GuildServiceProfileResp {
    @SerializedName("nickname")
    var nickname: String = ""

    @SerializedName("tiny_id")
    var tinyId: String = ""

    @SerializedName("avatar_url")
    var avatarUrl: String = ""
}

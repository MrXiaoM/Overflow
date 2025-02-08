package cn.evolvefield.onebot.sdk.response.guild

import com.google.gson.annotations.SerializedName

class GuildServiceProfileResp {
    @SerializedName("nickname")
    var nickname: String = ""

    @SerializedName("tiny_id")
    var tinyId: String = ""

    @SerializedName("avatar_url")
    var avatarUrl: String = ""
}

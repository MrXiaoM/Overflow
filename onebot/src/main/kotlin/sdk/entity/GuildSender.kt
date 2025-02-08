package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName

class GuildSender {
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName("tiny_id")
    var tinyId = ""
    @SerializedName("nickname")
    var nickname = ""
    @SerializedName("card")
    var card = ""
    @SerializedName("level")
    var level = ""
    @SerializedName("role")
    var role = ""
    @SerializedName("title")
    var title = ""
}

package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName

class GroupSender {
    @SerializedName("user_id")
    var userId = ""
    @SerializedName("nickname")
    var nickname = ""
    @SerializedName("card")
    var card = ""
    @SerializedName("role")
    var role = "member"
}

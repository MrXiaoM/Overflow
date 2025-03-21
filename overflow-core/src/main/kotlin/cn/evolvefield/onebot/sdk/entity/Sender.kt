package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName

class Sender {
    @SerializedName("user_id")
    var userId = ""
    @SerializedName("nickname")
    var nickname = ""
    @SerializedName("card")
    var card = ""
    @SerializedName("sex")
    var sex = ""
    @SerializedName("age")
    var age = 0
    @SerializedName("area")
    var area = ""
    @SerializedName("level")
    var level = ""
    @SerializedName("role")
    var role = ""
    @SerializedName("title")
    var title = ""
}

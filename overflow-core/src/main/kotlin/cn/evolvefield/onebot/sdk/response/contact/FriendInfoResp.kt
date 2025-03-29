package cn.evolvefield.onebot.sdk.response.contact

import com.google.gson.annotations.SerializedName

class FriendInfoResp  {
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName(value = "nickname", alternate = ["user_name"])
    var nickname = ""
    @SerializedName(value = "remark", alternate = ["user_remark"])
    var remark = ""
    @SerializedName("sex")
    var sex = "unknown"
    @SerializedName("age")
    var age = 0
    @SerializedName("email")
    var email = ""
    @SerializedName("level")
    var level = 0
}

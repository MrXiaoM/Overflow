package cn.evolvefield.onebot.sdk.response.contact

import com.google.gson.annotations.SerializedName

class FriendInfoResp  {
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName(value = "nickname", alternate = ["user_name"])
    var nickname = ""
    @SerializedName(value = "remark", alternate = ["user_remark"])
    var remark = ""
    @SerializedName(value = "level")
    var qqLevel = 0
    @SerializedName("sex")
    var sex = "unknown"
}

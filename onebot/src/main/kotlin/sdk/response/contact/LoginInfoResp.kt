package cn.evolvefield.onebot.sdk.response.contact

import com.google.gson.annotations.SerializedName

class LoginInfoResp {
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName("nickname")
    var nickname = ""
}

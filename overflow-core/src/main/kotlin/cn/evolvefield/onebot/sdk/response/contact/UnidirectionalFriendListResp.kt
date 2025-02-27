package cn.evolvefield.onebot.sdk.response.contact

import com.google.gson.annotations.SerializedName

class UnidirectionalFriendListResp {
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName("nickname")
    var nickname = ""
    @SerializedName("source")
    var source = ""
}

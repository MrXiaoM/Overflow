package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName

class CredentialsResp {
    @SerializedName("cookies")
    var cookies = ""
    @SerializedName("csrf_token", alternate = ["token"])
    var token = ""
}

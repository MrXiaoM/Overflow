package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class CredentialsResp {
    @SerializedName("cookies")
    var cookies = ""
    @SerializedName("csrf_token", alternate = ["token"])
    var token = ""
}

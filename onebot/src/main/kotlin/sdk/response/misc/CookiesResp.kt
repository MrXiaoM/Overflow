package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class CookiesResp {
    @SerializedName("cookies")
    var cookies = ""
}

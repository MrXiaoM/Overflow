package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class CheckUrlSafelyResp {
    @SerializedName("level")
    var level = 0
}

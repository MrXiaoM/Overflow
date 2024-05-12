package cn.evolvefield.onebot.sdk.action

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class ActionRaw {
    @SerializedName("status")
    var status = "failed"
    @SerializedName("retCode")
    var retCode = 1400
    @SerializedName("echo")
    var echo: String? = null
}

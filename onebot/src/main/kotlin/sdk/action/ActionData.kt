package cn.evolvefield.onebot.sdk.action

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class ActionData<T : Any> {
    @SerializedName("status")
    var status = "failed"
    @SerializedName("retcode")
    var retCode = 1400
    @SerializedName("data")
    var data: T? = null
    @SerializedName("echo")
    var echo: String? = null
}

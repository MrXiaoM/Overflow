package cn.evolvefield.onebot.sdk.action

import com.google.gson.annotations.SerializedName
import lombok.Data
import java.util.LinkedList

@Data
class ActionList<T : Any> {
    @SerializedName("status")
    var status = "failed"
    @SerializedName("retcode")
    var retCode: Int = 1004
    @SerializedName("data")
    var data: LinkedList<T>? = null
}

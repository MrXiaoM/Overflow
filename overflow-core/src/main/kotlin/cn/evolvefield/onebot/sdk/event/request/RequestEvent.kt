package cn.evolvefield.onebot.sdk.event.request

import cn.evolvefield.onebot.sdk.event.Event
import com.google.gson.annotations.SerializedName

open class RequestEvent : Event() {
    @SerializedName("request_type")
    var requestType = ""
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName(value = "comment", alternate = ["message"])
    var comment = ""
    @SerializedName("flag")
    var flag = ""
}

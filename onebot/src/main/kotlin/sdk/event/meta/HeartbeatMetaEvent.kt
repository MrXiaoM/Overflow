package cn.evolvefield.onebot.sdk.event.meta

import com.google.gson.annotations.SerializedName

class HeartbeatMetaEvent : MetaEvent() {
    init {
        metaEventType = "heartbeat"
    }
    @SerializedName("status")
    val status: Any = ""
    @SerializedName("interval")
    val interval: Long = 0
}

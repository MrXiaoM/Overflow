package cn.evolvefield.onebot.sdk.event.meta

import com.google.gson.annotations.SerializedName

class LifecycleMetaEvent : MetaEvent() {
    init {
        metaEventType = "lifecycle"
    }
    @SerializedName("sub_type")
    var subType = "" // enable、disable、connect
}

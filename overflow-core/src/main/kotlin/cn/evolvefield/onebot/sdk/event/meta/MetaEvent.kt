package cn.evolvefield.onebot.sdk.event.meta

import cn.evolvefield.onebot.sdk.event.Event
import com.google.gson.annotations.SerializedName

open class MetaEvent : Event() {
    init {
        postType = "meta_event"
    }
    @SerializedName("meta_event_type")
    var metaEventType = ""
}

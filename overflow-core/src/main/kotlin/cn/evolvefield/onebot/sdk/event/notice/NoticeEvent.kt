package cn.evolvefield.onebot.sdk.event.notice

import cn.evolvefield.onebot.sdk.event.Event
import com.google.gson.annotations.SerializedName

open class NoticeEvent : Event() {
    @SerializedName("notice_type")
    var noticeType = ""
    @SerializedName("user_id")
    var userId = 0L
}

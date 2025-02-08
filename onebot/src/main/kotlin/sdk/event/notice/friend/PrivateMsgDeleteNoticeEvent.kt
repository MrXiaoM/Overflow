package cn.evolvefield.onebot.sdk.event.notice.friend

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class PrivateMsgDeleteNoticeEvent : NoticeEvent() {
    @SerializedName("operator_id")
    var operatorId = 0L
    @SerializedName("message_id")
    var msgId = 0L
}

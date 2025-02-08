package cn.evolvefield.onebot.sdk.event.notice.group

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class GroupNameChangeNoticeEvent : NoticeEvent() {
    @SerializedName("name")
    var name = ""
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("operator_id")
    var operatorId = 0L
}

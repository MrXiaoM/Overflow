package cn.evolvefield.onebot.sdk.event.notice.group

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class GroupTitleChangeNoticeEvent : NoticeEvent() {
    @SerializedName("title_new")
    var titleNew = ""
    @SerializedName("title_old")
    var titleOld = ""
    @SerializedName("group_id")
    var groupId = 0L
}

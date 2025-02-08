package cn.evolvefield.onebot.sdk.event.notice.group

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class GroupCardChangeNoticeEvent : NoticeEvent() {
    @SerializedName("card_new")
    var cardNew = ""
    @SerializedName("card_old")
    var cardOld = ""
    @SerializedName("group_id")
    var groupId = 0L
}

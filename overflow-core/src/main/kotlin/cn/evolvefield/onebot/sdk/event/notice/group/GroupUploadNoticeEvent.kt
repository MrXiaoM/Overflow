package cn.evolvefield.onebot.sdk.event.notice.group

import cn.evolvefield.onebot.sdk.entity.File
import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class GroupUploadNoticeEvent : NoticeEvent() {
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("file")
    var file: File? = null
}

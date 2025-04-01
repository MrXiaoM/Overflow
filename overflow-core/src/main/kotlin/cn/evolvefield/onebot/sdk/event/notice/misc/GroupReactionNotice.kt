package cn.evolvefield.onebot.sdk.event.notice.misc

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class GroupReactionNotice : NoticeEvent() {
    @SerializedName("sub_type") // add, remove
    var subType = ""
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("message_id")
    var messageId = 0
    @SerializedName("operator_id")
    var operatorId = 0L
    @SerializedName("code", alternate = ["icon", "icon_id"])
    var code = ""
    @SerializedName("count")
    var count = 0
}

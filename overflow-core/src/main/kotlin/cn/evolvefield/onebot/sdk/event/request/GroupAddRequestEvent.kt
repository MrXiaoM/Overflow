package cn.evolvefield.onebot.sdk.event.request

import com.google.gson.annotations.SerializedName

class GroupAddRequestEvent : RequestEvent() {
    @SerializedName("sub_type")
    var subType = ""
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("invitor_id")
    var invitorId = 0L
}

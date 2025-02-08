package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName

open class GroupDataResp {
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("group_name")
    var groupName = ""
}

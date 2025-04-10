package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName

class GroupInfoResp : GroupDataResp() {
    @SerializedName("group_all_shut") // NapCat，全员禁言，-1 开启，0 关闭
    var groupAllShut = 0
    @SerializedName("group_memo")
    var groupMemo = ""
    @SerializedName("group_create_time")
    var groupCreateTime = 0
    @SerializedName("group_level")
    var groupLevel = 0
    @SerializedName("member_count")
    var memberCount: Int? = null
    @SerializedName("max_member_count")
    var maxMemberCount: Int? = null
}

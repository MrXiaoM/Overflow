package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
class GroupInfoResp : GroupDataResp() {
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

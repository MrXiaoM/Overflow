package cn.evolvefield.onebot.sdk.event.notice.group

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class GroupNotifyNoticeEvent : NoticeEvent() {
    @SerializedName("sub_type")
    var subType = ""
    @SerializedName("operator_id")
    var operatorId = 0L
    @SerializedName("target_id")
    var targetId = 0L
    @SerializedName("group_id")
    var groupId = 0L
}

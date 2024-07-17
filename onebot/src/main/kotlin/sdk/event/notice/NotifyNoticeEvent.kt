package cn.evolvefield.onebot.sdk.event.notice

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.ToString

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
class NotifyNoticeEvent : NoticeEvent() {
    @SerializedName("sub_type")
    var subType = ""
    @SerializedName("operator_id")
    var operatorId = 0L
    @SerializedName("target_id")
    var targetId = 0L
    @SerializedName("group_id")
    var groupId = 0L
    val realOperatorId: Long
        get() = operatorId.takeIf { it > 0 } ?: userId
}

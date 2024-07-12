package cn.evolvefield.onebot.sdk.event.request

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
class GroupAddRequestEvent : RequestEvent() {
    @SerializedName("sub_type")
    var subType = ""
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("invitor_id")
    var invitorId = 0L
}

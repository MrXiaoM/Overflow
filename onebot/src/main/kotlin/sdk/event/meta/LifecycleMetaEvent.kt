package cn.evolvefield.onebot.sdk.event.meta

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
class LifecycleMetaEvent : MetaEvent() {
    init {
        metaEventType = "lifecycle"
    }
    @SerializedName("sub_type")
    var subType = "" // enable、disable、connect
}

package cn.evolvefield.onebot.sdk.event.meta

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
class HeartbeatMetaEvent : MetaEvent() {
    init {
        metaEventType = "heartbeat"
    }
    @SerializedName("status")
    val status: Any = ""
    @SerializedName("interval")
    val interval: Long = 0
}

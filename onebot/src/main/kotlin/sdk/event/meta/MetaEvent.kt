package cn.evolvefield.onebot.sdk.event.meta

import cn.evolvefield.onebot.sdk.event.Event
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
open class MetaEvent : Event() {
    init {
        postType = "meta_event"
    }
    @SerializedName("meta_event_type")
    var metaEventType = ""
}

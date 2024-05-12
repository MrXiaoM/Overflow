package cn.evolvefield.onebot.sdk.event

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

/**
 * 事件上报
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
open class Event {
    @SerializedName("post_type")
    var postType: String = ""
    @SerializedName("type")
    var time: Long = 0
    @SerializedName("self_id")
    var selfId: Long = 0
}

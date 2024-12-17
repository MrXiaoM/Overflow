package cn.evolvefield.onebot.sdk.event

import cn.evolvefield.onebot.sdk.action.JsonContainer
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
open class Event : JsonContainer() {
    @SerializedName("post_type")
    var postType: String = ""
    @SerializedName("time")
    var time: Long = 0
    @SerializedName("self_id")
    var selfId: Long = 0

    /**
     * 自动转换毫秒时间戳（如果是）为秒时间戳，
     * 仅支持2000年以后的毫秒时间戳，2000年以前的认定为秒时间戳
     */
    fun timeInSecond(): Long {
        return if (time < 946656000000L) time
        else time / 1000L
    }
}

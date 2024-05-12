package cn.evolvefield.onebot.sdk.event.request

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
open class RequestEvent : Event() {
    @SerializedName("request_type")
    var requestType = ""
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName(value = "comment", alternate = ["message"])
    var comment = ""
    @SerializedName("flag")
    var flag = ""
}

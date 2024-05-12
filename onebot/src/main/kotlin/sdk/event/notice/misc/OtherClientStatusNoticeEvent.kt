package cn.evolvefield.onebot.sdk.event.notice.misc

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
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
class OtherClientStatusNoticeEvent : NoticeEvent() {
    @SerializedName("online")
    var online = false
    @SerializedName("client")
    var client: Clients? = null

    /**
     * 设备对象
     */
    @Data
    class Clients {
        @SerializedName("app_id")
        var appId = 0
        @SerializedName("platform")
        var platform = ""
        @SerializedName("device_name")
        var deviceName = ""
        @SerializedName("device_kind")
        var deviceKind = ""
    }
}

package cn.evolvefield.onebot.sdk.event.notice.misc

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class OtherClientStatusNoticeEvent : NoticeEvent() {
    @SerializedName("online")
    var online = false
    @SerializedName("client")
    var client: Clients? = null

    /**
     * 设备对象
     */
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

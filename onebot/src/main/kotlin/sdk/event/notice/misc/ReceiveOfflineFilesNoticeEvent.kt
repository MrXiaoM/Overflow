package cn.evolvefield.onebot.sdk.event.notice.misc

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class ReceiveOfflineFilesNoticeEvent : NoticeEvent() {
    @SerializedName("file")
    var file: File? = null

    /**
     * 文件对象
     */
    class File {
        @SerializedName("name")
        var name = ""
        @SerializedName("size")
        var size = 0L
        @SerializedName("url")
        var url = ""
    }
}

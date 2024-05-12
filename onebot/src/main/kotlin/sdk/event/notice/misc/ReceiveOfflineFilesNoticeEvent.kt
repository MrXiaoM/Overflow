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
class ReceiveOfflineFilesNoticeEvent : NoticeEvent() {
    @SerializedName("file")
    var file: File? = null

    /**
     * 文件对象
     */
    @Data
    class File {
        @SerializedName("name")
        var name = ""
        @SerializedName("size")
        var size = 0L
        @SerializedName("url")
        var url = ""
    }
}

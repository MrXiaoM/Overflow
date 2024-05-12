package cn.evolvefield.onebot.sdk.event.notice

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
open class NoticeEvent : Event() {
    @SerializedName("notice_type")
    var noticeType = ""
    @SerializedName("user_id")
    var userId = 0L
}

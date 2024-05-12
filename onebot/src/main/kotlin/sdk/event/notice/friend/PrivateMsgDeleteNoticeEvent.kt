package cn.evolvefield.onebot.sdk.event.notice.friend

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
class PrivateMsgDeleteNoticeEvent : NoticeEvent() {
    @SerializedName("operator_id")
    var operatorId = 0L
    @SerializedName("message_id")
    var msgId = 0L
}

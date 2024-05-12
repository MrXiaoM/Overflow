package cn.evolvefield.onebot.sdk.event.notice.friend

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class PrivatePokeNoticeEvent : NoticeEvent() {
    @SerializedName("sub_type")
    var subType = ""
    @SerializedName("sender_id")
    var senderId = 0L
    @SerializedName("target_id")
    var targetId = 0L
}

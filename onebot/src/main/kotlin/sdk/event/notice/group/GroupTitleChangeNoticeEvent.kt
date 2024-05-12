package cn.evolvefield.onebot.sdk.event.notice.group

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
class GroupTitleChangeNoticeEvent : NoticeEvent() {
    @SerializedName("title_new")
    var titleNew = ""
    @SerializedName("title_old")
    var titleOld = ""
    @SerializedName("group_id")
    var groupId = 0L
}

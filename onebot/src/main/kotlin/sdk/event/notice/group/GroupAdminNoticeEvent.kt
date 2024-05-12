package cn.evolvefield.onebot.sdk.event.notice.group

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
class GroupAdminNoticeEvent : NoticeEvent() {
    /**
     * set、unset
     * 事件子类型, 分别表示设置和取消管理
     */
    @SerializedName( "sub_type")
    var subType = ""

    /**
     * 群号
     */
    @SerializedName( "group_id")
    var groupId = 0L

    // 管理员QQ号 userId 已在 NoticeEvent 中实现
}

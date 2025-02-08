package cn.evolvefield.onebot.sdk.event.notice.group

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class GroupEssenceNoticeEvent : NoticeEvent() {
    /**
     * add,delete	添加为add,移出为delete
     */
    @SerializedName("sub_type")
    var subType = ""

    /**
     * 消息发送者ID
     */
    @SerializedName("sender_id")
    var senderId: Long = 0L

    /**
     * 操作者ID
     */
    @SerializedName("operator_id")
    var operatorId: Long = 0L

    /**
     * 消息ID
     */
    @SerializedName("message_id")
    var messageId: Int = 0

    /**
     * 群号
     */
    @SerializedName( "group_id")
    var groupId = 0L
}

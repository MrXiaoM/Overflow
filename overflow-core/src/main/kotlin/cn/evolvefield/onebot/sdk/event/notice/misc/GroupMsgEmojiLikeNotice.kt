package cn.evolvefield.onebot.sdk.event.notice.misc

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName
import java.util.LinkedList

class GroupMsgEmojiLikeNotice : NoticeEvent() {
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("message_id")
    var messageId = 0
    @SerializedName("likes")
    var likes = LinkedList<Like>()

    class Like {
        @SerializedName("emoji_id")
        var emojiId = ""
        @SerializedName("count")
        var count = 0
    }
}

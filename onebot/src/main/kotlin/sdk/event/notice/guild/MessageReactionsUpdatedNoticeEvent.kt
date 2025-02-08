package cn.evolvefield.onebot.sdk.event.notice.guild

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import com.google.gson.annotations.SerializedName

class MessageReactionsUpdatedNoticeEvent : NoticeEvent() {
    /**
     * 频道ID
     */
    @SerializedName("guild_id")
    var guildId = ""
    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    var channelId = ""
    /**
     * 消息ID
     */
    @SerializedName("message_id")
    var messageId = ""
    /**
     * 当前消息被贴表情列表
     */
    @SerializedName("current_reactions")
    var currentReactions: List<ReactionInfo> = mutableListOf()

    class ReactionInfo {
        /**
         * 表情ID
         */
        @SerializedName("emoji_id")
        var emojiId = ""
        /**
         * 表情对应数值ID
         */
        @SerializedName("emoji_index")
        var emojiIndex = 0
        /**
         * 表情类型
         */
        @SerializedName("emoji_type")
        var emojiType = 0
        /**
         * 表情名字
         */
        @SerializedName("emoji_name")
        var emojiName = ""
        /**
         * 当前表情被贴数量
         */
        @SerializedName("count")
        var count = 0
        /**
         * BOT是否点击
         */
        @SerializedName("clicked")
        var clicked = false
    }
}

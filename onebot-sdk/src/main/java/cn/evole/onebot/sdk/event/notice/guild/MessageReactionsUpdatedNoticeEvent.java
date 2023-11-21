package cn.evole.onebot.sdk.event.notice.guild;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class MessageReactionsUpdatedNoticeEvent extends NoticeEvent {

    /**
     * 频道ID
     */
    @SerializedName("guild_id")
    private String guildId;

    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    private String channelId;

    /**
     * 消息ID
     */
    @SerializedName("message_id")
    private String messageId;

    /**
     * 当前消息被贴表情列表
     */
    private List<ReactionInfo> currentReactions;

    @Data
    private static class ReactionInfo {

        /**
         * 表情ID
         */
        @SerializedName("emoji_id")
        private String emojiId;

        /**
         * 表情对应数值ID
         */
        @SerializedName("emoji_index")
        private int emojiIndex;

        /**
         * 表情类型
         */
        @SerializedName("emoji_type")
        private int emojiType;

        /**
         * 表情名字
         */
        @SerializedName("emoji_name")
        private String emojiName;

        /**
         * 当前表情被贴数量
         */
        @SerializedName("count")
        private int count;

        /**
         * BOT是否点击
         */
        @SerializedName("clicked")
        private boolean clicked;

    }

}

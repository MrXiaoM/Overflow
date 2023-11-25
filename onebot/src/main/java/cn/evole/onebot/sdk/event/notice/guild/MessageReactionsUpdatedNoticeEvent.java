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
    public String guildId;

    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    public String channelId;

    /**
     * 消息ID
     */
    @SerializedName("message_id")
    public String messageId;

    /**
     * 当前消息被贴表情列表
     */
    public List<ReactionInfo> currentReactions;

    @Data
    private static class ReactionInfo {

        /**
         * 表情ID
         */
        @SerializedName("emoji_id")
        public String emojiId;

        /**
         * 表情对应数值ID
         */
        @SerializedName("emoji_index")
        public int emojiIndex;

        /**
         * 表情类型
         */
        @SerializedName("emoji_type")
        public int emojiType;

        /**
         * 表情名字
         */
        @SerializedName("emoji_name")
        public String emojiName;

        /**
         * 当前表情被贴数量
         */
        @SerializedName("count")
        public int count;

        /**
         * BOT是否点击
         */
        @SerializedName("clicked")
        public boolean clicked;

    }

}

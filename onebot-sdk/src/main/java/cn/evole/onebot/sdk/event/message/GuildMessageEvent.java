package cn.evole.onebot.sdk.event.message;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 频道消息
 *
 * @author Alexskim
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class GuildMessageEvent extends MessageEvent {

    @SerializedName( "message_id")
    private String messageId;

    @SerializedName( "sub_type")
    private String subType;

    @SerializedName( "guild_id")
    private String guildId;

    @SerializedName( "channel_id")
    private String channelId;

    @SerializedName( "self_tiny_id")
    private String selfTinyId;

    @SerializedName( "sender")
    private Sender sender;

    /**
     * Sender Info
     */
    @Data
    public static class Sender {

        @SerializedName( "user_id")
        private long userId;

        @SerializedName( "tiny_id")
        private String tinyId;

        @SerializedName( "nickname")
        private String nickname;

    }

}

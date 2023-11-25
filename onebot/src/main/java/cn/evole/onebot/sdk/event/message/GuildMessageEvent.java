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
    public String messageId;

    @SerializedName( "sub_type")
    public String subType;

    @SerializedName( "guild_id")
    public String guildId;

    @SerializedName( "channel_id")
    public String channelId;

    @SerializedName( "self_tiny_id")
    public String selfTinyId;

    @SerializedName( "sender")
    public Sender sender;

    /**
     * Sender Info
     */
    @Data
    public static class Sender {

        @SerializedName( "user_id")
        public long userId;

        @SerializedName( "tiny_id")
        public String tinyId;

        @SerializedName( "nickname")
        public String nickname;

    }

}

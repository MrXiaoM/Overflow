package cn.evole.onebot.sdk.event.message;

import cn.evole.onebot.sdk.util.json.MessageEventAdapter;
import com.google.gson.annotations.JsonAdapter;
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
@JsonAdapter(MessageEventAdapter.class)
public class GuildMessageEvent extends MessageEvent {
    public String messageId;
    public String subType;
    public String guildId;
    public String channelId;
    public String selfTinyId;
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

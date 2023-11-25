package cn.evole.onebot.sdk.event.message;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 私聊消息
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class PrivateMessageEvent extends MessageEvent {

    @SerializedName( "message_id")
    public int messageId;

    @SerializedName( "sub_type")
    public String subType;

    @SerializedName( "sender")
    public PrivateSender privateSender;

    /**
     * sender信息
     */
    @Data
    public static class PrivateSender {

        @SerializedName( "user_id")
        public long userId;

        @SerializedName( "nickname")
        public String nickname;

        @SerializedName( "sex")
        public String sex;

        @SerializedName( "age")
        public int age;

    }

}

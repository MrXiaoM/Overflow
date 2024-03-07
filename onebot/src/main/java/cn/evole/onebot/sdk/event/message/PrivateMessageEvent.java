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
 * 私聊消息
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonAdapter(MessageEventAdapter.class)
public class PrivateMessageEvent extends MessageEvent {
    public int messageId;
    public String subType;
    public PrivateSender privateSender;
    public long groupId;
    /**
     * go-cqhttp: <a href="https://docs.go-cqhttp.org/reference/data_struct.html#post-message-tempsource">docs</a><br/>
     *     Group(0),
     *     Consultation(1),
     *     Seek(2),
     *     QQMovie(3),
     *     HotChat(4),
     *     VerifyMsg(6),
     *     Discussion(7),
     *     Dating(8),
     *     Contact(9),
     *     Unknown(-1)
     */
    public int tempSource;
    public String fromNick;
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

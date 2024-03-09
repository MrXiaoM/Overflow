package cn.evole.onebot.sdk.event.message;

import cn.evole.onebot.sdk.util.json.MessageEventAdapter;
import com.google.gson.annotations.JsonAdapter;
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
    public long groupId; // OpenShamrock
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
    public String fromNick; // OpenShamrock
    /**
     * sender信息
     */
    @Data
    @JsonAdapter(MessageEventAdapter.PrivateSender.class)
    public static class PrivateSender {
        public long userId;
        public long groupId; // go-cqhttp
        public String nickname;
        public String sex;
        public int age;
    }

}

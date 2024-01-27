package cn.evole.onebot.sdk.response.group;

import cn.evole.onebot.sdk.util.json.ForwardMsgAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author MrXiaoM
 */
@Data
@AllArgsConstructor
@JsonAdapter(ForwardMsgAdapter.class)
public class ForwardMsgResp {
    /**
     * 消息节点列表
     */
    public List<Node> message;
    /**
     * 消息节点
     */
    @Data
    @AllArgsConstructor
    public static class Node {
        public int time;
        public String messageType;
        public int messageId;
        public int realId;
        public long peerId;
        public long targetId;
        public Sender sender;
        public String message;
    }

    /**
     * 消息发送者
     */
    @Data
    @AllArgsConstructor
    public static class Sender {
        @SerializedName("user_id")
        public long userId;
        @SerializedName("nickname")
        public String nickname;
        @SerializedName("sex")
        public String sex;
        @SerializedName("age")
        public int age;
        @SerializedName("uid")
        public String uid;
    }
}

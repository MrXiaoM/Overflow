package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Created on 2023/11/25.
 *
 * @author MrXiaoM
 */
@Data
public class GetHistoryMsgResp {
    /**
     * 发送时间
     */
    @SerializedName("time")
    public int time;

    /**
     * 消息类型
     */
    @SerializedName("message_type")
    public int messageType;

    /**
     * 消息id
     */
    @SerializedName("message_id")
    public int messageId;

    /**
     * 消息真实id
     */
    @SerializedName("real_id")
    public int realId;

    /**
     * 发送者
     */
    @SerializedName("sender")
    public Sender sender;


    /**
     * 消息内容
     */
    @SerializedName("message")
    public String message;

    /**
     * 群号
     */
    @SerializedName("group_id")
    public long groupId;

    /**
     * 消息目标(私聊)
     */
    @SerializedName("target_id")
    public long targetId;

    /**
     * 消息接收者，群聊是群号，私聊时是目标QQ
     */
    @SerializedName("peer_id")
    public long peerId;

    /**
     * sender信息
     */
    @Data
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

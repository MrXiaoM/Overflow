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
    private int time;

    /**
     * 消息类型
     */
    @SerializedName("message_type")
    private int messageType;

    /**
     * 消息id
     */
    @SerializedName("message_id")
    private int messageId;

    /**
     * 消息真实id
     */
    @SerializedName("real_id")
    private int realId;

    /**
     * 发送者
     */
    @SerializedName("sender")
    private Sender sender;


    /**
     * 消息内容
     */
    @SerializedName("message")
    private String message;

    /**
     * 群号
     */
    @SerializedName("group_id")
    private long groupId;

    /**
     * 消息目标(私聊)
     */
    @SerializedName("target_id")
    private long targetId;

    /**
     * 消息接收者，群聊是群号，私聊时是目标QQ
     */
    @SerializedName("peer_id")
    private long peerId;

    /**
     * sender信息
     */
    @Data
    public static class Sender {

        @SerializedName("user_id")
        private long userId;

        @SerializedName("nickname")
        private String nickname;

        @SerializedName("sex")
        private String sex;

        @SerializedName("age")
        private int age;

        @SerializedName("uid")
        private String uid;
    }

}

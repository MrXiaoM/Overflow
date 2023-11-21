package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Created on 2022/9/6.
 *
 * @author cnlimiter
 */
@Data
public class GetMsgResp {

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
     * 发送时间
     */
    @SerializedName("time")
    private int time;

    /**
     * 消息内容
     */
    @SerializedName("message")
    private String message;

    /**
     * 原始消息内容
     */
    @SerializedName("raw_message")
    private String rawMessage;

    /**
     * sender信息
     */
    @Data
    public static class Sender {

        @SerializedName("user_id")
        private String userId;

        @SerializedName("nickname")
        private String nickname;

        @SerializedName("card")
        private String card;

        @SerializedName("sex")
        private String sex;

        @SerializedName("age")
        private int age;

        @SerializedName("area")
        private String area;

        @SerializedName("level")
        private String level;

        @SerializedName("role")
        private String role;

        @SerializedName("title")
        private String title;

    }

}

package cn.evole.onebot.sdk.response.group;

import cn.evole.onebot.sdk.util.json.MsgAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created on 2022/9/6.
 *
 * @author cnlimiter
 */
@Data
@AllArgsConstructor
@JsonAdapter(MsgAdapter.class)
public class GetMsgResp {

    /**
     * 消息id
     */
    public int messageId;

    /**
     * 消息真实id
     */
    public int realId;

    /**
     * 发送者
     */
    public Sender sender;

    /**
     * 发送时间
     */
    public int time;

    /**
     * 消息内容
     */
    public String message;

    /**
     * 原始消息内容
     */
    public String rawMessage;

    public long peerId;
    public long groupId;
    public long targetId;
    /**
     * sender信息
     */
    @Data
    public static class Sender {

        @SerializedName("user_id")
        public String userId;

        @SerializedName("nickname")
        public String nickname;

        @SerializedName("card")
        public String card;

        @SerializedName("sex")
        public String sex;

        @SerializedName("age")
        public int age;

        @SerializedName("area")
        public String area;

        @SerializedName("level")
        public String level;

        @SerializedName("role")
        public String role;

        @SerializedName("title")
        public String title;

    }

}

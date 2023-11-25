package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author MrXiaoM
 */
@Data
public class ForwardMsgResp {
    /**
     * 消息节点列表
     */
    @SerializedName("message")
    private List<Node> message;
    /**
     * 消息节点
     */
    @Data
    public static class Node {

        @SerializedName("user_id")
        private String userId;

        @SerializedName("nickname")
        private String nickname;

        /**
         * 消息内容
         */
        @SerializedName("content")
        private String content;
    }
}

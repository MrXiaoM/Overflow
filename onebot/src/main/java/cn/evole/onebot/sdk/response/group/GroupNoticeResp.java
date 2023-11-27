package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created on 2023/11/27.
 *
 * @author MrXiaoM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupNoticeResp {
    @SerializedName("sender_id")
    public long senderId;
    @SerializedName("publish_time")
    public long publishTime;
    @SerializedName("message")
    public Message message;

    @Data
    public static class Message {
        @SerializedName("text")
        public String text;
        @SerializedName("images")
        public List<Image> images;
    }
    @Data
    public static class Image {
        @SerializedName("id")
        public String id;
        @SerializedName("width")
        public String width;
        @SerializedName("height")
        public String height;
    }
}

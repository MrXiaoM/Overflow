package cn.evole.onebot.sdk.event.message;

import cn.evole.onebot.sdk.entity.Anonymous;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class GroupMessageEvent extends MessageEvent {

    @SerializedName( "message_id")
    public int messageId;

    @SerializedName( "sub_type")
    public String subType;

    @SerializedName( "group_id")
    public long groupId;

    @SerializedName( "anonymous")
    public Anonymous anonymous;

    @SerializedName( "sender")
    public GroupSender sender;

    /**
     * sender信息
     */
    @Data
    public static class GroupSender {

        @SerializedName( "user_id")
        public String userId;

        @SerializedName( "nickname")
        public String nickname;

        @SerializedName( "card")
        public String card;

        @SerializedName( "sex")
        public String sex;

        @SerializedName( "age")
        public int age;

        @SerializedName( "area")
        public String area;

        @SerializedName( "level")
        public String level;

        @SerializedName( "role")
        public String role;

        @SerializedName( "title")
        public String title;

    }

}

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
    private int messageId;

    @SerializedName( "sub_type")
    private String subType;

    @SerializedName( "group_id")
    private long groupId;

    @SerializedName( "anonymous")
    private Anonymous anonymous;

    @SerializedName( "sender")
    private GroupSender sender;

    /**
     * sender信息
     */
    @Data
    public static class GroupSender {

        @SerializedName( "user_id")
        private String userId;

        @SerializedName( "nickname")
        private String nickname;

        @SerializedName( "card")
        private String card;

        @SerializedName( "sex")
        private String sex;

        @SerializedName( "age")
        private int age;

        @SerializedName( "area")
        private String area;

        @SerializedName( "level")
        private String level;

        @SerializedName( "role")
        private String role;

        @SerializedName( "title")
        private String title;

    }

}

package cn.evole.onebot.sdk.event.message;

import cn.evole.onebot.sdk.entity.Anonymous;
import cn.evole.onebot.sdk.util.json.MessageEventAdapter;
import com.google.gson.annotations.JsonAdapter;
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
@JsonAdapter(MessageEventAdapter.class)
public class GroupMessageEvent extends MessageEvent {
    public int messageId;
    public String subType;
    public long groupId;
    public Anonymous anonymous;
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

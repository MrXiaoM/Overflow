package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberInfoResp {

    @SerializedName("group_id")
    private long groupId;

    @SerializedName("user_id")
    private long userId;

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

    @SerializedName("join_time")
    private int joinTime;

    @SerializedName("last_sent_time")
    private int lastSentTime;

    @SerializedName("level")
    private int level;

    @SerializedName("role")
    private String role;

    @SerializedName("unfriendly")
    private boolean unfriendly;

    @SerializedName("title")
    private String title;

    @SerializedName("title_expire_time")
    private long titleExpireTime;

    @SerializedName("card_changeable")
    private boolean cardChangeable;



}

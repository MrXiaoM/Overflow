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
    public long groupId;

    @SerializedName("user_id")
    public long userId;

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

    @SerializedName("join_time")
    public int joinTime;

    @SerializedName("last_sent_time")
    public int lastSentTime;

    @SerializedName("level")
    public int level;

    @SerializedName("role")
    public String role;

    @SerializedName("unfriendly")
    public boolean unfriendly;

    @SerializedName("title")
    public String title;

    @SerializedName("title_expire_time")
    public long titleExpireTime;

    @SerializedName("card_changeable")
    public boolean cardChangeable;

    @SerializedName("shut_up_timestamp")
    public long shutUpTimestamp;

}

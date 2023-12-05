package cn.evole.onebot.sdk.response.misc;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created on 2022/12/6.
 *
 * @author MrXiaoM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResp {

    @SerializedName("user_id")
    long uin;
    @SerializedName("user_name")
    String name;
    @SerializedName("user_displayname")
    String displayName;
    @SerializedName("user_remark")
    String remark;
    @SerializedName("mail")
    String mail;
    @SerializedName("find_method")
    String findMethod;
    @SerializedName("max_vote_cnt")
    short maxVoteCnt;
    @SerializedName("have_vote_cnt")
    short haveVoteCnt;
    @SerializedName("vip_list")
    List<VipInfo> vipList;
    @SerializedName("hobby_entry")
    String hobbyEntry;
    @SerializedName("level")
    int level;
    @SerializedName("birthday")
    long birthday;
    @SerializedName("login_day")
    long loginDay;
    @SerializedName("vote_cnt")
    long voteCnt;
    @SerializedName("qid")
    String qid;
    @SerializedName("is_school_verified")
    Boolean schoolVerified;
    @SerializedName("location")
    Location location;
    @SerializedName("cookie")
    byte[] cookie;
    @Data
    public static class VipInfo {
        /**
         * QQ_VIP,
         * SUPER_QQ,
         * SUPER_VIP,
         * QQ_VIDEO,
         * QQ_READING,
         * BIG_VIP,
         * YELLOW_VIP
         */
        String type;
        int level;
        @SerializedName("vip_type")
        int vipType;
        @SerializedName("template_id")
        long templateId;
    }
    @Data
    public static class Location {
        String city;
        String company;
        String country;
        String province;
        String hometown;
        String school;
    }
}

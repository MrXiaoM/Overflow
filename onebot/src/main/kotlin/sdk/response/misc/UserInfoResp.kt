package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class UserInfoResp {

    @SerializedName("user_id")
    var uin= 0L

    @SerializedName("user_name")
    var name = ""

    @SerializedName("user_displayname")
    var displayName = ""

    @SerializedName("user_remark")
    var remark = ""

    @SerializedName("mail")
    var mail = ""

    @SerializedName("find_method")
    var findMethod = ""

    @SerializedName("max_vote_cnt")
    var maxVoteCnt: Short = 0

    @SerializedName("have_vote_cnt")
    var haveVoteCnt: Short = 0

    @SerializedName("vip_list")
    var vipList: List<VipInfo> = mutableListOf()

    @SerializedName("hobby_entry")
    var hobbyEntry = ""

    @SerializedName("level")
    var level= 0

    @SerializedName("birthday")
    var birthday= 0L

    @SerializedName("login_day")
    var loginDay= 0L

    @SerializedName("vote_cnt")
    var voteCnt= 0L

    @SerializedName("qid")
    var qid = ""

    @SerializedName("is_school_verified")
    var schoolVerified = false

    @SerializedName("location")
    var location: Location? = null

    @SerializedName("cookie")
    var cookie: ByteArray = byteArrayOf()

    @Data
    class VipInfo {
        /**
         * QQ_VIP,
         * SUPER_QQ,
         * SUPER_VIP,
         * QQ_VIDEO,
         * QQ_READING,
         * BIG_VIP,
         * YELLOW_VIP
         */
        var type = ""
        var level= 0

        @SerializedName("vip_type")
        var vipType= 0

        @SerializedName("template_id")
        var templateId= 0L
    }

    @Data
    class Location {
        var city = ""
        var company = ""
        var country = ""
        var province = ""
        var hometown = ""
        var school = ""
    }
}

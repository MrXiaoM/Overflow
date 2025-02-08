package cn.evolvefield.onebot.sdk.response.contact

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

class StrangerInfoResp {
    /**
     * QQ 号
     */
    @SerializedName("user_id")
    var userId = 0L
    /**
     * 昵称
     */
    @SerializedName("nickname")
    var nickname = ""
    /**
     * 性别 male 或 female 或 unknown
     */
    @SerializedName("sex")
    var sex = ""
    /**
     * 年龄
     */
    @SerializedName("age")
    var age = 0
    /**
     * qid 身份卡
     */
    @SerializedName("qid")
    var qid = ""
    /**
     * 等级
     */
    @SerializedName("level")
    var level = 0
    /**
     * 在线天数？我猜的（
     */
    @SerializedName("login_days")
    var loginDays = 0
    @SerializedName("ext")
    var ext = JsonObject()
}

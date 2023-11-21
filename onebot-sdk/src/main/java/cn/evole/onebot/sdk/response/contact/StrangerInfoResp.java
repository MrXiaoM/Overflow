package cn.evole.onebot.sdk.response.contact;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
public class StrangerInfoResp {

    /**
     * QQ 号
     */
    @SerializedName("user_id")
    private long userId;

    /**
     * 昵称
     */
    @SerializedName("nickname")
    private String nickname;

    /**
     * 性别 male 或 female 或 unknown
     */
    @SerializedName("sex")
    private String sex;

    /**
     * 年龄
     */
    @SerializedName("age")
    private int age;

    /**
     * qid id 身份卡
     */
    @SerializedName("qid")
    private String qid;

    /**
     * 等级
     */
    @SerializedName("level")
    private int level;

    /**
     * 在线天数？我猜的（
     */
    @SerializedName("login_days")
    private int loginDays;

}

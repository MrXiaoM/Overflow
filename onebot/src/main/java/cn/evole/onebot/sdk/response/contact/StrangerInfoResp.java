package cn.evole.onebot.sdk.response.contact;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
@AllArgsConstructor
public class StrangerInfoResp {

    /**
     * QQ 号
     */
    @SerializedName("user_id")
    public long userId;

    /**
     * 昵称
     */
    @SerializedName("nickname")
    public String nickname;

    /**
     * 性别 male 或 female 或 unknown
     */
    @SerializedName("sex")
    public String sex;

    /**
     * 年龄
     */
    @SerializedName("age")
    public int age;

    /**
     * qid id 身份卡
     */
    @SerializedName("qid")
    public String qid;

    /**
     * 等级
     */
    @SerializedName("level")
    public int level;

    /**
     * 在线天数？我猜的（
     */
    @SerializedName("login_days")
    public int loginDays;

}

package cn.evole.onebot.sdk.response.contact;

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
@AllArgsConstructor
@NoArgsConstructor
public class FriendInfoResp {

    @SerializedName("user_id")
    public long userId;

    @SerializedName(value = "nickname", alternate = {"user_name"})
    public String nickname;

    @SerializedName(value = "remark", alternate = {"user_remark"})
    public String remark;

}

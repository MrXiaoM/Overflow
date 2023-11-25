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
    private long userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("remark")
    private String remark;

}

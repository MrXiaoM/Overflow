package cn.evole.onebot.sdk.response.contact;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class UnidirectionalFriendListResp {

    @SerializedName("user_id")
    public long userId;

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("source")
    public String source;

}

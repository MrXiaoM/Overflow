package cn.evole.onebot.sdk.response.contact;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class UnidirectionalFriendListResp {

    @SerializedName("user_id")
    private long userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("source")
    private String source;

}

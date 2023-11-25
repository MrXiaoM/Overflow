package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class GuildServiceProfileResp {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("tiny_id")
    private String tinyId;

    @SerializedName("avatar_url")
    private String avatarUrl;

}

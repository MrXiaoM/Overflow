package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class GuildServiceProfileResp {

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("tiny_id")
    public String tinyId;

    @SerializedName("avatar_url")
    public String avatarUrl;

}

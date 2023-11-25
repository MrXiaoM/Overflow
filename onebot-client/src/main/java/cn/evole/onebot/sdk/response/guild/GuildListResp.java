package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class GuildListResp {

    @SerializedName("guild_id")
    private String guildId;

    @SerializedName("guild_name")
    private String guildName;

    @SerializedName("guild_display_id")
    private String guildDisplayId;

}

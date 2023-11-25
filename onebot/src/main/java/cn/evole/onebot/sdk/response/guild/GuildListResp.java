package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class GuildListResp {

    @SerializedName("guild_id")
    public String guildId;

    @SerializedName("guild_name")
    public String guildName;

    @SerializedName("guild_display_id")
    public String guildDisplayId;

}

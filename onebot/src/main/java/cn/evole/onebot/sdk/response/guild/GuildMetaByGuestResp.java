package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class GuildMetaByGuestResp {

    @SerializedName("guild_id")
    public String guildId;

    @SerializedName("guild_name")
    public String guildName;

    @SerializedName("guild_profile")
    public String guildProfile;

    @SerializedName("create_time")
    public long createTime;

    @SerializedName("max_member_count")
    public long maxMemberCount;

    @SerializedName("max_robot_count")
    public long maxRobotCount;

    @SerializedName("max_admin_count")
    public long maxAdminCount;

    @SerializedName("member_count")
    public long memberCount;

    @SerializedName("owner_id")
    public String ownerId;

}

package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class GuildMetaByGuestResp {

    @SerializedName("guild_id")
    private String guildId;

    @SerializedName("guild_name")
    private String guildName;

    @SerializedName("guild_profile")
    private String guildProfile;

    @SerializedName("create_time")
    private long createTime;

    @SerializedName("max_member_count")
    private long maxMemberCount;

    @SerializedName("max_robot_count")
    private long maxRobotCount;

    @SerializedName("max_admin_count")
    private long maxAdminCount;

    @SerializedName("member_count")
    private long memberCount;

    @SerializedName("owner_id")
    private String ownerId;

}

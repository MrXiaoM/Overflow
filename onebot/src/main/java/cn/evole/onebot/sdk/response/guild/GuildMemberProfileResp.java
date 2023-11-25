package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
public class GuildMemberProfileResp {

    @SerializedName("tiny_id")
    public String tinyId;

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("join_time")
    public String joinTime;

    @SerializedName("roles")
    public List<RoleInfo> roles;

    @Data
    public static class RoleInfo {

        @SerializedName("role_id")
        public String roleId;

        @SerializedName("role_name")
        public String roleName;

    }

}

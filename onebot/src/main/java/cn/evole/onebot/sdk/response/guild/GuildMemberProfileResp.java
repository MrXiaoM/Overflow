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
    private String tinyId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("join_time")
    private String joinTime;

    @SerializedName("roles")
    private List<RoleInfo> roles;

    @Data
    public static class RoleInfo {

        @SerializedName("role_id")
        private String roleId;

        @SerializedName("role_name")
        private String roleName;

    }

}

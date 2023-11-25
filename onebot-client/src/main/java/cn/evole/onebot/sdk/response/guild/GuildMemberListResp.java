package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
public class GuildMemberListResp {

    /**
     * 成员列表
     */
    @SerializedName("members")
    private List<GuildMemberInfo> members;

    /**
     * 是否最终页
     */
    @SerializedName("finished")
    private boolean finished;

    /**
     * 翻页Token
     */
    @SerializedName("next_token")
    private String nextToken;

    @Data
    private static class GuildMemberInfo {

        /**
         * 成员ID
         */
        @SerializedName("tiny_id")
        private String tinyId;

        /**
         * 成员昵称
         */
        @SerializedName("nickname")
        private String nickname;

        /**
         * 成员头衔
         */
        @SerializedName("title")
        private String title;

        /**
         * 所在权限组ID
         * 默认情况下频道管理员的权限组ID为 2, 部分频道可能会另行创建, 需手动判断
         * 此接口仅展现最新的权限组, 获取用户加入的所有权限组请使用 get_guild_member_profile 接口
         */
        @SerializedName("role_id")
        private String roleId;

        /**
         * 所在权限组名称
         */
        @SerializedName("role_name")
        private String roleName;

    }

}

package cn.evole.onebot.sdk.response.guild;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
public class ChannelInfoResp {

    /**
     * 所属频道ID
     */
    @SerializedName("owner_guild_id")
    public String ownerGuildId;

    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    public String channelId;

    /**
     * 子频道类型
     */
    @SerializedName("channel_type")
    public int channelType;

    /**
     * 子频道名称
     */
    @SerializedName("channel_name")
    public String channelName;

    /**
     * 创建时间
     */
    @SerializedName("create_time")
    public long createTime;

    /**
     * 创建者ID
     */
    @SerializedName("creator_tiny_id")
    public String creatorTinyId;

    /**
     * 发言权限类型
     */
    @SerializedName("talk_permission")
    public int talkPermission;

    /**
     * 可视性类型
     */
    @SerializedName("visible_type")
    public int visibleType;

    /**
     * 当前启用的慢速模式Key
     */
    @SerializedName("current_slow_mode")
    public int currentSlowMode;

    /**
     * 频道内可用慢速模式类型列表
     */
    @SerializedName("slow_modes")
    public List<SlowModeInfo> slowModes;

    @Data
    private static class SlowModeInfo {

        /**
         * 慢速模式Key
         */
        @SerializedName("slow_mode_key")
        public int slowModeKey;

        /**
         * 慢速模式说明
         */
        @SerializedName("slow_mode_text")
        public String slowModeText;

        /**
         * 周期内发言频率限制
         */
        @SerializedName("speak_frequency")
        public int speakFrequency;

        /**
         * 单位周期时间, 单位秒
         */
        @SerializedName("slow_mode_circle")
        public int slowModeCircle;

    }

}

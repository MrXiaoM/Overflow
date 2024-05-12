package cn.evolvefield.onebot.sdk.response.guild

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class ChannelInfoResp {
    /**
     * 所属频道ID
     */
    @SerializedName("owner_guild_id")
    var ownerGuildID = ""
    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    var channelId = ""
    /**
     * 子频道类型
     */
    @SerializedName("channel_type")
    var channelType = 0
    /**
     * 子频道名称
     */
    @SerializedName("channel_name")
    var channelName = ""
    /**
     * 创建时间
     */
    @SerializedName("create_time")
    var createTime = 0L
    /**
     * 创建者ID
     */
    @SerializedName("creator_tiny_id")
    var creatorTinyId = ""
    /**
     * 发言权限类型
     */
    @SerializedName("talk_permission")
    var talkPermission = 0
    /**
     * 可视性类型
     */
    @SerializedName("visible_type")
    var visibleType = 0
    /**
     * 当前启用的慢速模式Key
     */
    @SerializedName("current_slow_mode")
    var currentSlowMode = 0
    /**
     * 频道内可用慢速模式类型列表
     */
    @SerializedName("slow_modes")
    var slowModes: List<SlowModeInfo> = mutableListOf()

    @Data
    class SlowModeInfo {
        /**
         * 慢速模式Key
         */
        @SerializedName("slow_mode_key")
        var slowModeKey = 0
        /**
         * 慢速模式说明
         */
        @SerializedName("slow_mode_text")
        var slowModeText = ""
        /**
         * 周期内发言频率限制
         */
        @SerializedName("speak_frequency")
        var speekFrequency = 0
        /**
         * 单位周期时间, 单位秒
         */
        @SerializedName("slow_mode_circle")
        var slowModeCircle = 0
    }
}

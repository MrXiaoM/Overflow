package cn.evolvefield.onebot.sdk.event.notice.guild

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import cn.evolvefield.onebot.sdk.response.guild.ChannelInfoResp
import com.google.gson.annotations.SerializedName

class ChannelUpdateNoticeEvent : NoticeEvent() {
    /**
     * 频道ID
     */
    @SerializedName("guild_id")
    var guildId = ""
    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    var channelId = ""
    /**
     * 操作者ID
     */
    @SerializedName("operator_id")
    var operatorId = ""
    /**
     * 更新前的频道信息
     */
    @SerializedName("old_info")
    var oldInfo: ChannelInfoResp? = null
    /**
     * 更新后的频道信息
     */
    @SerializedName("new_info")
    var newInfo: ChannelInfoResp? = null
}

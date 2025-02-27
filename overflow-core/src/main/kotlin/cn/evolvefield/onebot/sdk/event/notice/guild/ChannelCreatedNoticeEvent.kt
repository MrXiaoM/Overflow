package cn.evolvefield.onebot.sdk.event.notice.guild

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import cn.evolvefield.onebot.sdk.response.guild.ChannelInfoResp
import com.google.gson.annotations.SerializedName

class ChannelCreatedNoticeEvent : NoticeEvent() {
    /**
     * 频道ID
     */
    @SerializedName("guild_id")
    var guildId = ""
    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    var channelID = ""
    /**
     * 操作者ID
     */
    @SerializedName("operator_id")
    var operatorId = ""
    /**
     * 频道信息
     */
    @SerializedName("channel_info")
    var channelInfo: ChannelInfoResp? = null
}

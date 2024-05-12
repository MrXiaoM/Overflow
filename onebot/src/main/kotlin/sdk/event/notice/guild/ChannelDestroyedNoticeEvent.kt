package cn.evolvefield.onebot.sdk.event.notice.guild

import cn.evolvefield.onebot.sdk.event.notice.NoticeEvent
import cn.evolvefield.onebot.sdk.response.guild.ChannelInfoResp
import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
class ChannelDestroyedNoticeEvent : NoticeEvent() {
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
     * 频道信息
     */
    @SerializedName("channel_info")
    var channelInfo: ChannelInfoResp? = null
}

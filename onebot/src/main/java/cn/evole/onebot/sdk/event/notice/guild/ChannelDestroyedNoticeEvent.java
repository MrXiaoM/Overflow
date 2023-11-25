package cn.evole.onebot.sdk.event.notice.guild;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
import cn.evole.onebot.sdk.response.guild.ChannelInfoResp;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class ChannelDestroyedNoticeEvent extends NoticeEvent {

    /**
     * 频道ID
     */
    @SerializedName("guild_id")
    public String guildId;

    /**
     * 子频道ID
     */
    @SerializedName("channel_id")
    public String channelId;

    /**
     * 操作者ID
     */
    @SerializedName("operator_id")
    public String operatorId;

    /**
     * 频道信息
     */
    @SerializedName("channel_info")
    private ChannelInfoResp channelInfo;

}

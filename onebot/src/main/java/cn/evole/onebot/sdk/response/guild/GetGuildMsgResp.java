package cn.evole.onebot.sdk.response.guild;

import cn.evole.onebot.sdk.event.message.GuildMessageEvent;
import com.google.gson.annotations.SerializedName;
import lombok.Data;


@Data
public class GetGuildMsgResp {

    @SerializedName("guild_id")
    public String guildId;

    @SerializedName("channel_id")
    public String channelId;

    @SerializedName("message")
    public String message;

    @SerializedName("message_id")
    public String messageId;

    @SerializedName("message_seq")
    public int messageSeq;

    @SerializedName("message_source")
    public String messageSource;

    @SerializedName("sender")
    private GuildMessageEvent.GuildSender sender;

    @SerializedName("time")
    public long time;

}

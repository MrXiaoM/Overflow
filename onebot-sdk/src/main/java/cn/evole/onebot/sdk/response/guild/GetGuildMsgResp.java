package cn.evole.onebot.sdk.response.guild;

import cn.evole.onebot.sdk.event.message.GuildMessageEvent;
import com.google.gson.annotations.SerializedName;
import lombok.Data;


@Data
public class GetGuildMsgResp {

    @SerializedName("guild_id")
    private String guildId;

    @SerializedName("channel_id")
    private String channelId;

    @SerializedName("message")
    private String message;

    @SerializedName("message_id")
    private String messageId;

    @SerializedName("message_seq")
    private int messageSeq;

    @SerializedName("message_source")
    private String messageSource;

    @SerializedName("sender")
    private GuildMessageEvent.Sender sender;

    @SerializedName("time")
    private long time;

}

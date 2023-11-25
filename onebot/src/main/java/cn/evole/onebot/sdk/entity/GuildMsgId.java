package cn.evole.onebot.sdk.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 17:39
 * Version: 1.0
 */
@Data
public class GuildMsgId {
    @SerializedName( "message_id")
    public String messageId;

}

package cn.evole.onebot.sdk.entity;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/13 22:06
 * Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgId {
    @SerializedName( "message_id")
    private int messageId;
}

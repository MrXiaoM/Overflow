package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class EssenceMsgResp {

    @SerializedName("sender_id")
    public long senderId;

    @SerializedName("sender_nick")
    public String senderNick;

    @SerializedName("sender_time")
    public long senderTime;

    @SerializedName("operator_id")
    public long operatorId;

    @SerializedName("operator_nick")
    public String operatorNick;

    @SerializedName("operator_time")
    public String operatorTime;

    @SerializedName("message_id")
    public int messageId;

}

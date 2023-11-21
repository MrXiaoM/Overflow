package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author cnlimiter
 */
@Data
public class EssenceMsgResp {

    @SerializedName("sender_id")
    private long senderId;

    @SerializedName("sender_nick")
    private String senderNick;

    @SerializedName("sender_time")
    private long senderTime;

    @SerializedName("operator_id")
    private long operatorId;

    @SerializedName("operator_nick")
    private String operatorNick;

    @SerializedName("operator_time")
    private String operatorTime;

    @SerializedName("message_id")
    private int messageId;

}

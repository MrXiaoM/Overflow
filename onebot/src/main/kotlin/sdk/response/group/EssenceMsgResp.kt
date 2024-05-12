package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName

class EssenceMsgResp {
    @SerializedName("sender_id")
    var senderId = 0L
    @SerializedName("sender_nick")
    var senderNick = ""
    @SerializedName("sender_time")
    var senderTime = 0L
    @SerializedName("operator_id")
    var operatorId = 0L
    @SerializedName("operator_nick")
    var operatorNick = ""
    @SerializedName("operator_time")
    var operatorTime = ""
    @SerializedName("message_id")
    var messageId = 0
}

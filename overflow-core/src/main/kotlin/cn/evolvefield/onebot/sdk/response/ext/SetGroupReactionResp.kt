package cn.evolvefield.onebot.sdk.response.ext

import com.google.gson.annotations.SerializedName

/**
 * AstralGocq
 */
class SetGroupReactionResp {
    @SerializedName("message_id")
    var messageId = 0L
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("message_seq")
    var messageSequence = 0L
}

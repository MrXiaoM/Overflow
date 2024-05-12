package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GuildMsgId {
    @SerializedName("message_id")
    var messageId: String? = null
}

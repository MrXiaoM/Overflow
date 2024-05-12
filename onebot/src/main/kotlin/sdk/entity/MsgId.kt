package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class MsgId {
    @SerializedName("message_id")
    var messageId: Int? = null
}

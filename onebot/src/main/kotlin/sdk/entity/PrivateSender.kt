package cn.evolvefield.onebot.sdk.entity

import cn.evolvefield.sdk.util.json.MessageEventAdapter
import com.google.gson.annotations.JsonAdapter
import lombok.Data

@Data
@JsonAdapter(MessageEventAdapter.PrivateSenderAdapter::class)
class PrivateSender {
    var userId = 0L
    var groupId = 0L // go-cqhttp
    var nickname = ""
    var sex = ""
    var age = 0
}

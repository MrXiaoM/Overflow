package cn.evolvefield.onebot.sdk.response.group

import cn.evolvefield.onebot.sdk.entity.Sender
import cn.evolvefield.onebot.sdk.util.json.MsgAdapter
import com.google.gson.annotations.JsonAdapter

@JsonAdapter(MsgAdapter::class)
class GetMsgResp {
    var messageId = 0
    var realId = 0
    lateinit var sender: Sender
    var time = 0
    var isJsonMessage = false
    var message = ""
    var rawMessage = ""
    var peerId = 0L
    var groupId = 0L
    var targetId = 0L
}

package cn.evolvefield.onebot.sdk.response.group

import cn.evolvefield.onebot.sdk.util.json.ForwardMsgAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

/**
 * @author MrXiaoM
 */
@JsonAdapter(ForwardMsgAdapter::class)
class ForwardMsgResp {
    /**
     * 消息节点列表
     */
    var message: List<Node> = mutableListOf()
    /**
     * 消息节点
     */
    class Node {
        var time = 0
        var messageType = ""
        var messageId = 0
        var realId = 0
        var peerId = 0L
        var targetId = 0L
        var sender: Sender? = null
        var message = ""
    }

    /**
     * 消息发送者
     */
    class Sender {
        @SerializedName("user_id")
        var userId = 0L
        @SerializedName("nickname")
        var nickname = ""
        @SerializedName("sex")
        var sex = ""
        @SerializedName("age")
        var age = 0
        @SerializedName("uid")
        var uid = ""
    }
}

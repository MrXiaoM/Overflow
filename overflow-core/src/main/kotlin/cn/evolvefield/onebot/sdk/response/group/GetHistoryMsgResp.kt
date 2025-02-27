package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName

class GetHistoryMsgResp {
    /**
     * 发送时间
     */
    @SerializedName("time")
    var time = 0
    /**
     * 消息类型
     */
    @SerializedName("message_type")
    var messageType = 0
    /**
     * 消息id
     */
    @SerializedName("message_id")
    var messageId = 0
    /**
     * 消息真实id
     */
    @SerializedName("real_id")
    var realId = 0
    /**
     * 发送者
     */
    @SerializedName("sender")
    var sender: Sender? = null
    /**
     * 消息内容
     */
    @SerializedName("message")
    var message = ""
    /**
     * 群号
     */
    @SerializedName("group_id")
    var groupId = 0L
    /**
     * 消息目标(私聊)
     */
    @SerializedName("target_id")
    var targetId = 0L
    /**
     * 消息接收者，群聊是群号，私聊时是目标QQ
     */
    @SerializedName("peer_id")
    var peerId = 0L

    /**
     * sender信息
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

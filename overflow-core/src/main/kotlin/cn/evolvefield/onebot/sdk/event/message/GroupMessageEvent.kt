package cn.evolvefield.onebot.sdk.event.message

import cn.evolvefield.onebot.sdk.entity.Anonymous
import cn.evolvefield.onebot.sdk.entity.GroupSender
import cn.evolvefield.onebot.sdk.util.json.MessageEventAdapter
import com.google.gson.annotations.JsonAdapter

@JsonAdapter(MessageEventAdapter::class)
class GroupMessageEvent : MessageEvent() {
    var messageId: Int = 0
    var subType: String = ""
    var groupId = 0L
    var anonymous: Anonymous? = null
    var sender: GroupSender? = null
}

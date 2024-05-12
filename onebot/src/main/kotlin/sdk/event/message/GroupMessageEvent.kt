package cn.evolvefield.onebot.sdk.event.message

import cn.evolvefield.onebot.sdk.entity.Anonymous
import cn.evolvefield.onebot.sdk.entity.GroupSender
import cn.evolvefield.onebot.sdk.util.json.MessageEventAdapter
import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonAdapter(MessageEventAdapter::class)
class GroupMessageEvent : MessageEvent() {
    var messageId: Int = 0
    var subType: String = ""
    var groupId = 0L
    var anonymous: Anonymous? = null
    var sender: GroupSender? = null
}

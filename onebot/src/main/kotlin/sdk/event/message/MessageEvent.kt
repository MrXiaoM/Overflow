package cn.evolvefield.onebot.sdk.event.message

import cn.evolvefield.onebot.sdk.event.Event
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
open class MessageEvent : Event() {
    var messageType = ""
    var userId = 0L
    var message = ""
    var rawMessage = ""
    var font = 0
}

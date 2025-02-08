package cn.evolvefield.onebot.sdk.event.message

import cn.evolvefield.onebot.sdk.event.Event

open class MessageEvent : Event() {
    var messageType = ""
    var userId = 0L
    var isJsonMessage = false
    var message = ""
    var rawMessage = ""
    var font = 0
}

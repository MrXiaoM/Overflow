package cn.evolvefield.onebot.sdk.event

class IgnoreEvent() : Event() {
    init {
        postType = "IGNORED"
        time = System.currentTimeMillis()
    }
    constructor(id: Long) : this() {
        selfId = id
    }
}
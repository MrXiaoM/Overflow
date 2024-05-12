package cn.evolvefield.onebot.sdk.event

import cn.evolvefield.onebot.sdk.event.Event
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.experimental.SuperBuilder

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
class IgnoreEvent constructor() : Event() {
    init {
        postType = "IGNORED"
        time = System.currentTimeMillis()
    }
    constructor(id: Long) : this() {
        selfId = id
    }
}
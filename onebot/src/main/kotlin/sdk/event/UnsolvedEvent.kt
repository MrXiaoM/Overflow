package cn.evolvefield.onebot.sdk.event

import cn.evolvefield.onebot.sdk.event.Event
import lombok.Data
import lombok.EqualsAndHashCode

/**
 * 未成功解析的事件
 *
 * @author MrXiaoM
 */
@Data
@EqualsAndHashCode(callSuper = true)
class UnsolvedEvent : Event() {
    var jsonString: String = "{}"
}

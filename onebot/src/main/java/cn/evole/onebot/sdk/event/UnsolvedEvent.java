package cn.evole.onebot.sdk.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 未成功解析的事件
 *
 * @author MrXiaoM
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnsolvedEvent extends Event {
    public String jsonString;
}

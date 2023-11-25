package cn.evole.onebot.sdk.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/3 13:20
 * Version: 1.0
 */
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class IgnoreEvent extends Event{
    public IgnoreEvent(){
        this.setPostType("IGNORED");
        this.setTime(System.currentTimeMillis());
    }
    public IgnoreEvent(long id){
        this.setPostType("IGNORED");
        this.setTime(System.currentTimeMillis());
        this.setSelfId(id);
    }


}

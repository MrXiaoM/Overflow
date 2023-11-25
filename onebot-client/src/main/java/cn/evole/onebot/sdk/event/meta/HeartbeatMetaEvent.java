package cn.evole.onebot.sdk.event.meta;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/3 13:28
 * Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class HeartbeatMetaEvent extends MetaEvent {
    @SerializedName("status")
    private Object status;

    @SerializedName("interval")
    private long interval;


    public HeartbeatMetaEvent(long selfId, long time, Object status, long interval){
        this.setSelfId(selfId);
        this.setTime(time);
        this.status = status;
        this.interval = interval;
    }

    @Override
    public void setMetaEventType(String metaEventType) {
        super.setMetaEventType("heartbeat");
    }

}

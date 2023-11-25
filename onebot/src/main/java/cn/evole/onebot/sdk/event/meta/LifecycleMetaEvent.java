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
 * Date: 2022/10/3 13:21
 * Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class LifecycleMetaEvent extends MetaEvent {

    @SerializedName("sub_type")
    private String subType;// enable、disable、connect

    public LifecycleMetaEvent(long selfId, String subType, long time){
        this.setSelfId(selfId);
        this.subType= subType;
        this.setTime(time);
    }

    @Override
    public void setMetaEventType(String metaEventType) {
        super.setMetaEventType("lifecycle");
    }
}

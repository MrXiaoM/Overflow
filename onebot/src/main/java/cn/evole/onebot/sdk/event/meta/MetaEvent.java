package cn.evole.onebot.sdk.event.meta;

import cn.evole.onebot.sdk.event.Event;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/7 1:09
 * Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class MetaEvent extends Event {
    @SerializedName("meta_event_type")
    public String metaEventType;

    @Override
    public void setPostType(String postType) {
        super.setPostType("meta_event");
    }
}

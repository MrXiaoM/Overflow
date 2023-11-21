package cn.evole.onebot.sdk.event;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 事件上报
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Event {

    @SerializedName("post_type")
    private String postType;

    @SerializedName( "time")
    private long time;

    @SerializedName( "self_id")
    private long selfId;

}

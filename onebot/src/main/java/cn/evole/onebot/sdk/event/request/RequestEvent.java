package cn.evole.onebot.sdk.event.request;

import cn.evole.onebot.sdk.event.Event;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class RequestEvent extends Event {

    @SerializedName( "request_type")
    private String requestType;

    @SerializedName( "user_id")
    private long userId;

    @SerializedName( "comment")
    private String comment;

    @SerializedName( "flag")
    private String flag;

}

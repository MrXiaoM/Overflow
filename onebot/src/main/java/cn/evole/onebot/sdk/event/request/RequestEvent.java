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
    public String requestType;

    @SerializedName( "user_id")
    public long userId;

    @SerializedName( "comment")
    public String comment;

    @SerializedName( "flag")
    public String flag;

}

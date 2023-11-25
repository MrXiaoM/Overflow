package cn.evole.onebot.sdk.event.message;

import cn.evole.onebot.sdk.entity.MsgChainBean;
import cn.evole.onebot.sdk.event.Event;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class MessageEvent extends Event {

    @SerializedName( "message_type")
    public String messageType;

    @SerializedName( "user_id")
    public long userId;

    @SerializedName( "message")
    public String message;

    @SerializedName( "raw_message")
    public String rawMessage;

    @SerializedName( "font")
    public int font;

    public List<MsgChainBean> arrayMsg;

}

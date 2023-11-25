package cn.evole.onebot.sdk.event.message;

import cn.evole.onebot.sdk.entity.MsgChainBean;
import cn.evole.onebot.sdk.event.Event;
import com.google.gson.JsonArray;
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
    public String messageType;
    public long userId;
    public String message;
    public String rawMessage;
    public int font;
}

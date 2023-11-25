package cn.evole.onebot.sdk.event.notice.friend;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrivatePokeNoticeEvent extends NoticeEvent {

    @SerializedName("sub_type")
    public String subType;

    @SerializedName("sender_id")
    public long senderId;

    @SerializedName("target_id")
    public long targetId;



}

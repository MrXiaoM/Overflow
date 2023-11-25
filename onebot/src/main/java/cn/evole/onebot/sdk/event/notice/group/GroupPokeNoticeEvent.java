package cn.evole.onebot.sdk.event.notice.group;

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
public class GroupPokeNoticeEvent extends NoticeEvent {

    @SerializedName("sub_type")
    private String subType;

    @SerializedName("target_id")
    private long targetId;

    @SerializedName("group_id")
    private long groupId;


}

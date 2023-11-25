package cn.evole.onebot.sdk.event.notice.group;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created on 2023/11/25.
 *
 * @author MrXiaoM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupNotifyNoticeEvent extends NoticeEvent {

    @SerializedName("sub_type")
    public String subType;

    @SerializedName("operator_id")
    public long operatorId;

    @SerializedName("target_id")
    public long targetId;

    @SerializedName("group_id")
    public long groupId;

}

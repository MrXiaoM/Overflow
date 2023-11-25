package cn.evole.onebot.sdk.event.notice.group;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/1 23:30
 * Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class GroupEssenceNoticeEvent extends NoticeEvent {
    @SerializedName( "sub_type")
    public String  subType;//add,delete	添加为add,移出为delete
    @SerializedName( "sender_id")
    public Long  senderId	;//消息发送者ID
    @SerializedName( "operator_id")
    public Long operatorId;//操作者ID
    @SerializedName( "message_id")
    public Long messageId	;//消息ID
}

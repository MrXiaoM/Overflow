package cn.evole.onebot.sdk.event.notice.friend;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
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
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class FriendAddNoticeEvent extends NoticeEvent {

}

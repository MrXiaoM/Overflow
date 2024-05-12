package cn.evolvefield.onebot.sdk.event.notice.friend

import cn.evolvefield.onebot.sdk.event.request.RequestEvent
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
class FriendAddNoticeEvent : RequestEvent()

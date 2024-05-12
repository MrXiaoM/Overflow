package cn.evolvefield.onebot.sdk.event.request

import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.experimental.SuperBuilder

@Data
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
class FriendAddRequestEvent : RequestEvent()

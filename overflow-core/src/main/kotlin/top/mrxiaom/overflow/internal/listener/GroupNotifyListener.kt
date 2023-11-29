package top.mrxiaom.overflow.internal.listener

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evole.onebot.sdk.event.message.GroupMessageEvent.GroupSender
import cn.evole.onebot.sdk.event.notice.group.GroupNotifyNoticeEvent
import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.asOnebot
import top.mrxiaom.overflow.contact.BotWrapper
import top.mrxiaom.overflow.contact.GroupWrapper
import top.mrxiaom.overflow.contact.MemberWrapper
import top.mrxiaom.overflow.message.OnebotMessages

internal class GroupNotifyListener(
    val bot: BotWrapper
) : EventListener<GroupNotifyNoticeEvent> {
    @OptIn(MiraiInternalApi::class)
    override suspend fun onMessage(e: GroupNotifyNoticeEvent) {
        val group = bot.groups.getOrFail(e.groupId)
        when(e.subType) {
            "poke" -> {
                // TODO: 戳一戳事件
            }
        }
    }
}

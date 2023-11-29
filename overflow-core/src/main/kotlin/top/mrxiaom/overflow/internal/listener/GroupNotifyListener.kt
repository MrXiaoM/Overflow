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
import top.mrxiaom.overflow.internal.asOnebot
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.contact.GroupWrapper
import top.mrxiaom.overflow.internal.contact.MemberWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages

internal class GroupNotifyListener(
    val bot: BotWrapper
) : EventListener<GroupNotifyNoticeEvent> {
    @OptIn(MiraiInternalApi::class)
    override suspend fun onMessage(e: GroupNotifyNoticeEvent) {
        val group = bot.getGroup(e.groupId) ?: kotlin.run {
            val data = bot.impl.getGroupInfo(e.groupId, false).data ?: throw IllegalStateException("无法取得群信息")
            bot.updateGroup(GroupWrapper(bot, data))
        }
        when (e.subType) {
            "poke" -> {
                val operator = group.members[e.operatorId] ?: throw IllegalStateException("群 ${group.id} 戳一戳 无法获取操作者")
                val target = group.members[e.targetId] ?: throw IllegalStateException("群 ${group.id} 戳一戳 无法获取目标")
                // TODO: 戳一戳无法获取被戳一方的动作、后缀信息
                NudgeEvent(operator, target, group, "拍了拍", "").broadcast()
            }
        }
    }
}

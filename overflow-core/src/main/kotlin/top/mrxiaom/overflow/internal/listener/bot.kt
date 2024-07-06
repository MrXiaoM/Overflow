@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.sdk.event.UnsolvedEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.sdk.event.notice.group.GroupNotifyNoticeEvent
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.OverflowAPI.Companion.logger
import top.mrxiaom.overflow.event.UnsolvedOnebotEvent
import top.mrxiaom.overflow.internal.scope
import top.mrxiaom.overflow.internal.utils.bot
import top.mrxiaom.overflow.internal.utils.group

internal fun addBotListeners() {
    listOf(
        UnsolvedEventListener(),
        NotifyNoticeListener(),

    ).forEach(EventBus::addListener)
}

internal class NotifyNoticeListener : EventListener<GroupNotifyNoticeEvent> {
    @OptIn(MiraiInternalApi::class)
    override suspend fun onMessage(e: GroupNotifyNoticeEvent) {
        val bot = e.bot ?: return
        when (e.subType) {
            "poke" -> {
                // Lagrange: notice -> notify -> poke 不仅仅适用于群聊
                if (e.groupId == 0L) {
                    val operator = bot.getFriend(e.operatorId)
                        ?: throw IllegalStateException("好友 ${e.operatorId} 戳一戳事件 操作者不在好友列表内")
                    val target = if (e.targetId == bot.id) bot.asFriend
                    else throw IllegalStateException("好友 ${e.operatorId} 戳一戳事件 出现预料中的错误: 目标好友不是机器人")
                    // TODO: 戳一戳无法获取被戳一方的动作、后缀信息
                    bot.eventDispatcher.broadcastAsync(NudgeEvent(operator, target, operator, "拍了拍", ""))
                } else {
                    val group = bot.group(e.groupId)
                    val operator = group.queryMember(e.operatorId)
                        ?: throw IllegalStateException("群 ${group.id} 戳一戳事件 无法获取操作者")
                    val target = group.queryMember(e.targetId)
                        ?: throw IllegalStateException("群 ${group.id} 戳一戳事件 无法获取目标")
                    // TODO: 戳一戳无法获取被戳一方的动作、后缀信息
                    bot.eventDispatcher.broadcastAsync(NudgeEvent(operator, target, group, "拍了拍", ""))
                }
            }
        }
    }
}

internal class UnsolvedEventListener : EventListener<UnsolvedEvent> {
    override suspend fun onMessage(e: UnsolvedEvent) {
        Overflow.scope.launch {
            UnsolvedOnebotEvent(e.selfId, e.jsonString, e.time).broadcast()
        }
    }
}

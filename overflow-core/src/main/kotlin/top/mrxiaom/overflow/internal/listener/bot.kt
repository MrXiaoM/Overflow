@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.sdk.event.UnsolvedEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.sdk.event.notice.NotifyNoticeEvent
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.event.UnsolvedOnebotEvent
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.scope
import top.mrxiaom.overflow.internal.utils.bot
import top.mrxiaom.overflow.internal.utils.group

internal fun addBotListeners() {
    listOf(
        UnsolvedEventListener(),
        NotifyNoticeListener(),

    ).forEach(EventBus::addListener)
}

internal class NotifyNoticeListener : EventListener<NotifyNoticeEvent> {
    @OptIn(MiraiInternalApi::class)
    override suspend fun onMessage(e: NotifyNoticeEvent) {
        val bot = e.bot ?: return
        when (e.subType) {
            "poke" -> {
                val operatorId = e.realOperatorId
                if (bot.checkId(operatorId) {
                    "%onebot 返回了异常的数值 operator_id=%value"
                }) return
                // Lagrange: notice -> notify -> poke 不仅仅适用于群聊
                if (e.groupId == 0L) {
                    val operator = bot.getFriend(operatorId)
                        ?: throw IllegalStateException("好友 $operatorId 戳一戳事件 操作者不在好友列表内")
                    // val target = if (e.targetId == bot.id) bot.asFriend
                    // else throw IllegalStateException("好友 $operatorId 戳一戳事件 出现预料中的错误: 目标好友不是机器人")
                    val target = bot.asFriend // 有的 Onebot 实现不一定提供 target_id
                    // TODO: 戳一戳无法获取被戳一方的动作、后缀信息
                    bot.eventDispatcher.broadcastAsync(NudgeEvent(operator, target, operator, "拍了拍", ""))
                } else {
                    val group = bot.group(e.groupId)
                    val operator = group.queryMember(operatorId)
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
            UnsolvedOnebotEvent(e.selfId, e.jsonString, e.timeInSecond()).broadcast()
        }
    }
}

internal fun BotWrapper.checkId(id: Long, msg: () -> String): Boolean {
    return (id <= 0).also {
        if (it) {
            logger.warning(msg()
                    .replace("%onebot", "${impl.appName} v${impl.appVersion}")
                    .replace("%value", id.toString())
            )
        }
    }
}

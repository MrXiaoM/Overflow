package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.client.handler.EventBus.listen
import cn.evolvefield.onebot.client.handler.EventBus.listenNormally
import cn.evolvefield.onebot.sdk.event.UnsolvedEvent
import cn.evolvefield.onebot.sdk.event.notice.NotifyNoticeEvent
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.NudgeEvent
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.event.UnsolvedOnebotEvent
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.scope
import top.mrxiaom.overflow.internal.utils.group

internal fun addBotListeners() {
    listen<NotifyNoticeEvent>("poke") { e ->
        // 戳一戳通知
        val operatorId = e.realOperatorId
        if (checkId(operatorId, "%onebot 返回了异常的数值 operator_id=%value")) return@listen
        // Lagrange: notice -> notify -> poke 不仅仅适用于群聊
        if (e.groupId == 0L) {
            val operator = bot.getFriend(operatorId)
                ?: throw IllegalStateException("好友 $operatorId 戳一戳事件 操作者不在好友列表内")
            val target = bot.asFriend // 有的 Onebot 实现不一定提供 target_id，所以直接使用机器人实例
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
    listenNormally<UnsolvedEvent> { e ->
        // 未处理 Onebot 事件
        Overflow.scope.launch {
            UnsolvedOnebotEvent(e.selfId, e.jsonString, e.timeInSecond()).broadcast()
        }
    }
}
internal fun BotWrapper.checkId(id: Long, msg: String): Boolean {
    return (id <= 0).also {
        if (it) logger.warning(msg
            .replace("%onebot", "${impl.appName} v${impl.appVersion}")
            .replace("%value", id.toString())
        )
    }
}

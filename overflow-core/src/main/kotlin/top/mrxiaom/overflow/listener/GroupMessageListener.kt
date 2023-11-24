package top.mrxiaom.overflow.listener

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.listener.EventListener

internal class GroupMessageListener(
    val bot: Bot
) : EventListener<GroupMessageEvent> {
    override suspend fun onMessage(e: GroupMessageEvent) {
        // TODO 构建 mirai 事件并广播
    }
}
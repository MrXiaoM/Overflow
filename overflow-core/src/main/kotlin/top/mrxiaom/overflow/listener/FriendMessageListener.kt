package top.mrxiaom.overflow.listener

import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.listener.EventListener

internal class FriendMessageListener(
    val bot: Bot
) : EventListener<PrivateMessageEvent> {
    override suspend fun onMessage(e: PrivateMessageEvent) {
        // TODO 构建 mirai 事件并广播
    }
}
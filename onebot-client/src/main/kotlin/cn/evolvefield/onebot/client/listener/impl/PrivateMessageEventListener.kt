package cn.evolvefield.onebot.client.listener.impl

import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.client.handler.DefaultEventHandler

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 16:29
 * Version: 1.0
 */
class PrivateMessageEventListener : DefaultEventHandler<PrivateMessageEvent>() {
    override suspend fun onMessage(e: PrivateMessageEvent) {
        //处理逻辑
        val message = e.message
        val split = message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val key = split[0]
        val handler = getHandler(key)
        handler.handle(e)
    }
}
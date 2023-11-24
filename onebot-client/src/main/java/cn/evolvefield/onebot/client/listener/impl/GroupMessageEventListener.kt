package cn.evolvefield.onebot.client.listener.impl

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.client.handler.DefaultEventHandler

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 16:15
 * Version: 1.0
 */
class GroupMessageEventListener : DefaultEventHandler<GroupMessageEvent>() {
    override fun onMessage(t: GroupMessageEvent) {
        //处理逻辑
        val message = t.message
        val split = message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val key = split[0]
        val handler = getHandler(key)
        handler.handle(t)
    }
}
package cn.evolvefield.onebot.client.listener

import cn.evole.onebot.sdk.event.Event

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 16:11
 * Version: 1.0
 */
abstract class SimpleEventListener<T : Event> : EnableEventListener<T>()
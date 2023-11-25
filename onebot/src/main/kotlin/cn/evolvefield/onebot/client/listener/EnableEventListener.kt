package cn.evolvefield.onebot.client.listener

import cn.evole.onebot.sdk.event.Event

/**
 * 提供是否开启插件
 * @param <T>
</T> */
abstract class EnableEventListener<T : Event> : EventListener<T> {
    var enable = true //默认开启
    fun enable(): Boolean {
        return enable
    }
}
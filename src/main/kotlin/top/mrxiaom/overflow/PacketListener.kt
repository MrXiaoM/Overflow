package top.mrxiaom.overflow

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import top.mrxiaom.overflow.events.PacketReceiveEvent

class PacketListener : SimpleListenerHost() {

    @EventHandler
    suspend fun PacketReceiveEvent.handle() {

    }
}
package top.mrxiaom.overflow.events

import net.mamoe.mirai.event.AbstractEvent
import top.mrxiaom.overflow.packet.IPacket

class PacketReceiveEvent(
    val packet: IPacket
) : AbstractEvent()

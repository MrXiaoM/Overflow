package top.mrxiaom.overflow

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.broadcast
import top.mrxiaom.overflow.events.PacketReceiveEvent
import top.mrxiaom.overflow.packet.PacketRegistry
import top.mrxiaom.overflow.utils.readUTF

class NetworkHandler : ChannelInboundHandlerAdapter() {
    private val scope = MainScope()

    override fun channelRead(ctx: ChannelHandlerContext, any: Any) {
        val msg = any as ByteBuf
        var packetId = "<unknown>"
        try {
            packetId = msg.readUTF()
            val packet = PacketRegistry.createPacket(packetId)
            packet.read(msg)

            scope.launch { PacketReceiveEvent(packet).broadcast() }
        } catch (t: Throwable) {
            instance.logger.error("接收包 $packetId 时发生一个异常", t)
        }
        msg.release()
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        instance.logger.error("Overflow 发生一个异常", cause)
        ctx?.close()
    }
}

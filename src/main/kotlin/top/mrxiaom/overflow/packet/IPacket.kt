package top.mrxiaom.overflow.packet

import io.netty.buffer.ByteBuf
import net.mamoe.mirai.utils.createInstanceOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmName

interface IPacket {
    fun read(data: ByteBuf)
    fun write(data: ByteBuf)
}

annotation class Registry(
    val packetId: String
)

object PacketRegistry {
    val registry = mutableMapOf<String, KClass<*>>()

    inline fun <reified T : IPacket> register(packet: KClass<T>) {
        val id = getPacketId(packet)
        if (registry.containsKey(id)) error("数据包Id `$packet` 已被注册")
        registry[id] = packet
    }

    fun getPacketId(clazz: KClass<*>): String {
        val info = clazz.findAnnotation<Registry>() ?: error("类 ${clazz.jvmName} 没有注解 @Registry")
        return info.packetId
    }

    fun createPacket(packetId: String): IPacket {
        val clazz = registry[packetId] ?: error("数据包 $packetId 不存在")
        val obj = clazz.createInstanceOrNull() ?: error("数据包 $packetId 应当存在一个无参构造函数")
        if (obj !is IPacket) error("数据包 $packetId 类型错误")
        return obj
    }
}
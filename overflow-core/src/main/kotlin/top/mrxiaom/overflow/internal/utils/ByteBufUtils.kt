package top.mrxiaom.overflow.internal.utils

import io.netty.buffer.ByteBuf

fun ByteBuf.readUTF(): String {
    val length = this.readInt()
    val bytes = this.readBytes(length)
    return bytes.array().toString(Charsets.UTF_8).also { bytes.release() }
}
fun ByteBuf.writeUTF(s: String) {
    writeInt(s.length)
    writeBytes(s.toByteArray(Charsets.UTF_8))
}
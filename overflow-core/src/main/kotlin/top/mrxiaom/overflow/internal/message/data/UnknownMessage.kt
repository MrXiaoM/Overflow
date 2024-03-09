package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.AbstractPolymorphicMessageKey
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.utils.createFileIfNotExists
import net.mamoe.mirai.utils.safeCast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private fun currentTime() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

@Serializable
@SerialName(UnknownMessage.SERIAL_NAME)
internal data class UnknownMessage(
    val msgType: String,
    val data: String
) : MessageContent {
    fun printLog(): UnknownMessage = apply {
        val logFile = File("logs/unknown_messages.log")
        logFile.createFileIfNotExists()
        logFile.appendText("${currentTime()} I/UnknownMessage: ${contentToString()}\n")
    }

    override fun contentToString(): String {
        return "[overflow,unknown:$msgType,$data}]"
    }

    override fun toString(): String = contentToString()

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, UnknownMessage>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "UnknownMessage"
    }
}
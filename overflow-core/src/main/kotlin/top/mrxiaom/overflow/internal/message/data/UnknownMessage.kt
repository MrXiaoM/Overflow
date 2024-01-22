package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.message.data.AbstractPolymorphicMessageKey
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.utils.createFileIfNotExists
import net.mamoe.mirai.utils.safeCast
import top.mrxiaom.overflow.message.data.ContactRecommend
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private fun currentTime() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

@Serializable
@SerialName(UnknownMessage.SERIAL_NAME)
internal data class UnknownMessage(
    val type: String,
    val data: String
) : MessageContent {
    init {
        val logFile = File("logs/unknown_messages.log")
        logFile.createFileIfNotExists()
        logFile.appendText("${currentTime()} I/UnknownMessage: ${contentToString()}\n")
    }

    override fun contentToString(): String {
        return "[overflow,unknown:$type,$data}]"
    }

    override fun toString(): String = contentToString()

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, UnknownMessage>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "UnknownMessage"
    }
}
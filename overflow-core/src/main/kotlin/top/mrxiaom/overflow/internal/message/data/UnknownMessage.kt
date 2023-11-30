package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.utils.createFileIfNotExists
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private fun currentTime() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

@Serializable
data class UnknownMessage(
    val type: String,
    val data: JsonObject
) : MessageContent {
    init {
        val logFile = File("logs/unknown_messages.log")
        logFile.createFileIfNotExists()
        logFile.appendText("${currentTime()} I/UnknownMessage: ${contentToString()}\n")
    }
    
    override fun contentToString(): String {
        data.toString()
        return "[overflow,unknown:$type,${Json.encodeToString(data)}]"
    }

    override fun toString(): String = contentToString()
}
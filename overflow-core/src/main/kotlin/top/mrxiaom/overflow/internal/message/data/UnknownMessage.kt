package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.message.data.MessageContent

@Serializable
data class UnknownMessage(
    val type: String,
    val data: JsonObject
) : MessageContent {
    override fun contentToString(): String {
        data.toString()
        return "[overflow,unknown:$type,${Json.encodeToString(data)}]"
    }

    override fun toString(): String = contentToString()
}
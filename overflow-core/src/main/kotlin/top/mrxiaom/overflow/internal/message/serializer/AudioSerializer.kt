package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Audio
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.audioFromFile
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.data.WrappedAudio
import top.mrxiaom.overflow.message.BuildMessageContext

class AudioSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "record"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is Audio
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        context.add(audioFromFile(data["file"].string))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? Audio ?: return false
        context.add("record") {
            put("file", element.onebotFile)
        }
        return true
    }

    private val Audio.onebotFile: String
        get() = (this as? WrappedAudio)?.file ?: ""
}

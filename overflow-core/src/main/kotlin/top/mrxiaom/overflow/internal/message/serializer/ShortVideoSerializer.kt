package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.ShortVideo
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.OnebotMessages.videoFromFile
import top.mrxiaom.overflow.internal.message.data.WrappedVideo
import top.mrxiaom.overflow.message.BuildMessageContext

class ShortVideoSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "video"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is ShortVideo
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        context.add(videoFromFile(data["file"].string))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? ShortVideo ?: return false
        context.add("video") {
            put("file", element.onebotFile)
        }
        return true
    }

    private val ShortVideo.onebotFile: String
        get() = (this as? WrappedVideo)?.file ?: ""
}
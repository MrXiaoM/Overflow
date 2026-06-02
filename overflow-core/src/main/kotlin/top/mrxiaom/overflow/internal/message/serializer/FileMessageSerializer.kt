package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.long
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.data.WrappedFileMessage
import top.mrxiaom.overflow.message.BuildMessageContext

class FileMessageSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "file"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is FileMessage
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        // OpenShamrock
        //val sub = data["sub"].string
        //val biz = data["biz"].int
        val size = (data["size"] ?: data["file_size"])?.long ?: 0L
        //val expire = data["expire"].int
        val name = (data["name"] ?: data["file"])?.string ?: ""
        val id = (data["id"] ?: data["file_id"]).string
        val url = data["url"].string

        context.add(WrappedFileMessage(id, 0, name, size, url))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        // 文件只允许通过接口上传，不允许通过消息链发送
        return true
    }
}

package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.flash
import top.mrxiaom.overflow.internal.message.OnebotMessages.imageFromFile
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.data.WrappedImage
import top.mrxiaom.overflow.message.BuildMessageContext

class ImageSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "image"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is Image || message is FlashImage
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val image = imageFromFile((data["url"] ?: data["file"]).string)
        if (data["type"].string == "flash") {
            context.add(image.flash())
        } else {
            context.add(image)
        }

        /**
         * 保存图片原始 Json 数据到 [WrappedImage.rawJson]
         */
        if (image is WrappedImage) {
            image.rawJson = data
        }
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val image = message as? Image
        val flash = message as? FlashImage
        if (image != null) {
            context.add("image") {
                put("file", image.onebotFile)
            }
            return true
        }
        if (flash != null) {
            context.add("image") {
                put("file", flash.onebotFile)
                put("type", "flash")
            }
            return true
        }
        return false
    }

    private val Image.onebotFile: String
        get() = imageId
    private val FlashImage.onebotFile: String
        get() = image.onebotFile
}
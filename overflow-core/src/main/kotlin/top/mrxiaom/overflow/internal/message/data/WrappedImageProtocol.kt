package top.mrxiaom.overflow.internal.message.data


import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.message.data.InternalImageProtocol
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.utils.base64Length
import top.mrxiaom.overflow.internal.utils.lengthToString

@OptIn(MiraiInternalApi::class)
internal class WrappedImageProtocol : InternalImageProtocol {
    @OptIn(MiraiExperimentalApi::class)
    override fun createImage(
        imageId: String,
        size: Long,
        type: ImageType,
        width: Int,
        height: Int,
        isEmoji: Boolean
    ): Image {
        return WrappedImage(imageId, type, size, width, height)
    }

    override suspend fun isUploaded(
        bot: Bot,
        md5: ByteArray,
        size: Long,
        context: Contact?,
        type: ImageType,
        width: Int,
        height: Int
    ): Boolean {
        return true
    }
}
@Serializable
@MiraiExperimentalApi
data class WrappedImage(
    val url: String,
    override val imageType: ImageType,
    override val size: Long,
    override val width: Int,
    override val height: Int,
): Image {
    private val _stringValue: String? by lazy(LazyThreadSafetyMode.NONE) {
        val file = if (url.startsWith("base64://") && url.length > 60) {
            val s = url.substring(9)
            val len = base64Length(s)
            "base64://${s.substring(0, 32)}... (about ${lengthToString(len)})"
        } else url
        "[overflow:image,url=$file,isEmoji=$isEmoji]"
    }
    override val imageId: String = url

    override fun contentToString(): String = if (isEmoji) {
        "[动画表情]"
    } else {
        "[图片]"
    }

    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:image:").append(imageId).append("]")
    }

    override fun toString(): String  = _stringValue!!
}
package top.mrxiaom.overflow.internal.message.data


import cn.evolvefield.onebot.client.util.fileType
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.calculateImageMd5ByImageId
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.message.data.InternalImageProtocol
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.toUHexString
import top.mrxiaom.overflow.internal.utils.base64Length
import top.mrxiaom.overflow.internal.utils.lengthToString
import java.util.*

@OptIn(MiraiInternalApi::class)
internal class WrappedImageProtocol : InternalImageProtocol {
    private val whateverBotId: Long
        get() = Bot.instances.firstOrNull()?.id ?: 10001L
    internal fun friendImageId(imageId: String): String {
        val md5 = calculateImageMd5ByImageId(imageId)
        //    /1234567890-3666252994-EFF4427CE3D27DB6B1D9A8AB72E7A29C
        return "/000000000-000000000-${md5.toUHexString("")}"
    }
    @OptIn(MiraiExperimentalApi::class)
    override fun createImage(
        imageId: String,
        size: Long,
        type: ImageType,
        width: Int,
        height: Int,
        isEmoji: Boolean
    ): Image {
        return when {
            imageId matches Image.IMAGE_ID_REGEX -> {
                // TODO: image cache
                // if (size == 0L && width == 0 && height == 0) {
                //     findExistImageByCache(imageId)?.let { return it }
                // }
                val url = "http://gchat.qpic.cn/gchatpic_new/$whateverBotId/0-0-$imageId/0?term=2"
                WrappedImage(url, imageId, type, size, width, height)
            }

            imageId matches Image.IMAGE_RESOURCE_ID_REGEX_1 -> {
                val url = "http://c2cpicdw.qpic.cn/offpic_new/$whateverBotId${friendImageId(imageId)}/0?term=2"
                WrappedImage(url, imageId, type, size, width, height)
            }

            imageId matches Image.IMAGE_RESOURCE_ID_REGEX_2 -> {
                val url = "http://c2cpicdw.qpic.cn/offpic_new/$whateverBotId${friendImageId(imageId)}/0?term=2"
                WrappedImage(url, imageId, type, size, width, height)
            }

            else -> {
                // Onebot
                // base64:// file:/// http:// https://
                return WrappedImage(imageId, "!$imageId", type, size, width, height)
            }
        }
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
internal data class WrappedImage(
    val url: String,
    override val imageId: String,
    override val imageType: ImageType,
    override val size: Long,
    override val width: Int,
    override val height: Int,
): Image {
    private val _stringValue: String? by lazy(LazyThreadSafetyMode.NONE) {
        val fileString = if (imageId.startsWith("!base64://") && url.length > 32) {
            val s = url.removePrefix("base64://")
            val len = base64Length(s)
            val type = Base64.getDecoder().decode(s).fileType ?: "*"
            "${url.substring(0, 32)}... (${if (type == "*") "" else "$type, "}about ${lengthToString(len)})"
        } else imageId.removePrefix("!")
        "[overflow:image:$fileString]"
    }

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
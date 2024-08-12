@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.message.data

import cn.evolvefield.onebot.client.util.fileType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.AbstractPolymorphicMessageKey
import net.mamoe.mirai.message.data.MessageKey
import net.mamoe.mirai.message.data.OnlineShortVideo
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.safeCast
import top.mrxiaom.overflow.internal.utils.base64Length
import top.mrxiaom.overflow.internal.utils.lengthToString
import top.mrxiaom.overflow.spi.MediaURLService
import top.mrxiaom.overflow.spi.MediaURLService.Companion.queryVideoUrl
import java.util.*

@Serializable
@SerialName(WrappedVideo.SERIAL_NAME)
internal data class WrappedVideo(
    val file: String,
    override val filename: String = if (file.startsWith("base64://")) "base64" else file.substringAfterLast("/"),
    override val videoId: String = filename,
) : OnlineShortVideo {
    private val _stringValue: String by lazy(LazyThreadSafetyMode.NONE) {
        val fileString = if (urlForDownload.startsWith("base64://") && urlForDownload.length > 32) {
            val s = urlForDownload.replace("base64://", "")
            val len = base64Length(s)
            val type = Base64.getDecoder().decode(s).fileType ?: "*"
            "${urlForDownload.substring(0, 32)}... (${if (type == "*") "" else "$type, "}about ${lengthToString(len)})"
        } else urlForDownload
        "[overflow:video,file=$fileString]"
    }
    override val fileFormat: String = "mp4"
    override val fileMd5: ByteArray = ByteArray(16)
    override val fileSize: Long by lazy {
        if (!urlForDownload.startsWith("base64://")) 0
        else base64Length(urlForDownload.substring(9))
    }
    override val urlForDownload: String
        get() {
            val extUrl = MediaURLService.instances.queryVideoUrl(this)
            return extUrl ?: file
        }

    override fun contentToString(): String {
        return "[视频消息]"
    }

    override fun toString(): String = _stringValue

    override val key: MessageKey<WrappedAudio> get() = WrappedAudio
    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, WrappedVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedVideo"
    }
}
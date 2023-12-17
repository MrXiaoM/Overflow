package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.AbstractPolymorphicMessageKey
import net.mamoe.mirai.message.data.OnlineShortVideo
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.safeCast
import top.mrxiaom.overflow.internal.utils.base64Length

@Serializable
data class WrappedVideo(
    val file: String
) : OnlineShortVideo {
    override val fileFormat: String = "mp4"
    override val fileMd5: ByteArray = ByteArray(16)
    override val fileSize: Long by lazy {
        if (!file.startsWith("base64://")) 0
        else base64Length(file.substring(9))
    }
    override val filename: String = if (file.startsWith("base64://")) "base64" else file.substringAfterLast("/")
    override val urlForDownload: String = file
    override val videoId: String = filename

    override fun contentToString(): String {
        return "[视频消息]"
    }

    override fun toString(): String = "[overflow:video,file=$filename]"

    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, WrappedVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedVideo"
    }
}
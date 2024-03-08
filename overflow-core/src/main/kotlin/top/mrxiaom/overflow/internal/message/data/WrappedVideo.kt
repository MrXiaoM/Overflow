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
import java.util.*

@Serializable
@SerialName(WrappedVideo.SERIAL_NAME)
internal data class WrappedVideo(
    val file: String
) : OnlineShortVideo {
    private val _stringValue: String by lazy(LazyThreadSafetyMode.NONE) {
        val fileString = if (file.startsWith("base64://") && file.length > 32) {
            val s = file.replace("base64://", "")
            val len = base64Length(s)
            val type = Base64.getDecoder().decode(s).fileType ?: "*"
            "${file.substring(0, 32)}... (${if (type == "*") "" else "$type, "}about ${lengthToString(len)})"
        } else file
        "[overflow:video,file=$fileString]"
    }
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

    override fun toString(): String = _stringValue

    override val key: MessageKey<WrappedAudio> get() = WrappedAudio
    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, WrappedVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedVideo"
    }
}
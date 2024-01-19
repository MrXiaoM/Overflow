package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast
import top.mrxiaom.overflow.internal.utils.base64Length
import top.mrxiaom.overflow.internal.utils.lengthToString

@Serializable
internal data class WrappedAudio(
    override val urlForDownload: String,
    override val length: Long,
): OnlineAudio, OfflineAudio {
    public companion object Key :
        AbstractPolymorphicMessageKey<Audio, WrappedAudio>(Audio, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedAudio"
    }

    private val _stringValue: String? by lazy(LazyThreadSafetyMode.NONE) {
        val fileString = if (file.startsWith("base64://") && file.length > 60) {
            val s = file.substring(9)
            val len = base64Length(s)
            "base64://${s.substring(0, 32)}... (about ${lengthToString(len)})"
        } else file
        "[overflow:audio,file=$fileString]"
    }
    override val codec: AudioCodec = AudioCodec.AMR
    override val extraData: ByteArray? = null
    override val fileMd5: ByteArray = ByteArray(16)
    override val fileSize: Long by lazy {
        length.takeIf { it > 0 } ?: if (!file.startsWith("base64://")) 0
        else base64Length(file.substring(9))
    }
    override val filename: String = urlForDownload.substringAfterLast("/")
    val file: String = urlForDownload

    override fun toString(): String = _stringValue!!
}
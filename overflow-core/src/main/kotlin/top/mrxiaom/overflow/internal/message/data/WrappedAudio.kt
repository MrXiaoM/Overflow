package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast

@Serializable
data class WrappedAudio(
    override val urlForDownload: String,
    override val length: Long,
): OnlineAudio, OfflineAudio {
    public companion object Key :
        AbstractPolymorphicMessageKey<Audio, WrappedAudio>(Audio, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedAudio"
    }

    override val codec: AudioCodec = AudioCodec.AMR
    override val extraData: ByteArray? = null
    override val fileMd5: ByteArray = ByteArray(16)
    override val fileSize: Long = 0
    override val filename: String = urlForDownload.substringAfterLast("/")
    val file: String = urlForDownload

    override fun toString(): String = "[overflow:audio,file=$filename]"
}
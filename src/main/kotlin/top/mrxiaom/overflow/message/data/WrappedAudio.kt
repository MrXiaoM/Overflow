package top.mrxiaom.overflow.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.AbstractPolymorphicMessageKey
import net.mamoe.mirai.message.data.Audio
import net.mamoe.mirai.message.data.AudioCodec
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.utils.safeCast

@Serializable
data class WrappedAudio(
    val file: String
): OfflineAudio {
    public companion object Key :
        AbstractPolymorphicMessageKey<Audio, WrappedAudio>(Audio, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedAudio"
    }

    override val codec: AudioCodec = AudioCodec.AMR
    override val extraData: ByteArray? = null
    override val fileMd5: ByteArray = ByteArray(16)
    override val fileSize: Long = 0
    override val filename: String = file

    override fun toString(): String = "[overflow:audio,file=$filename]"
}
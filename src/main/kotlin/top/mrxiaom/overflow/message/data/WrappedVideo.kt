package top.mrxiaom.overflow.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.AbstractPolymorphicMessageKey
import net.mamoe.mirai.message.data.OnlineShortVideo
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.safeCast

@Serializable
data class WrappedVideo(
    val file: String
) : OnlineShortVideo {
    override val fileFormat: String = ""
    override val fileMd5: ByteArray = ByteArray(16)
    override val fileSize: Long = 0

    override val filename: String = file
    override val urlForDownload: String = file
    override val videoId: String = file

    override fun contentToString(): String {
        return "[视频消息]"
    }

    override fun toString(): String = "[overflow:video,file=$filename]"

    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, WrappedVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedVideo"
    }
}
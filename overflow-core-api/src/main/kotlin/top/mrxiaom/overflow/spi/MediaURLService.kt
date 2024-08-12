@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.spi

import net.mamoe.mirai.message.data.Audio
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.spi.BaseService
import net.mamoe.mirai.spi.SpiServiceLoader
import top.mrxiaom.overflow.contact.RemoteBot

interface MediaURLService : BaseService {

    suspend fun queryImageUrl(bot: RemoteBot, image: Image): String?

    fun queryAudioUrl(audio: Audio): String?

    fun queryVideoUrl(video: ShortVideo): String?

    companion object {
        private val loader = SpiServiceLoader(MediaURLService::class)

        internal val instances: List<MediaURLService>
            get() = loader.allServices.sortedBy { it.priority }

        suspend fun List<MediaURLService>.queryImageUrl(bot: RemoteBot, image: Image): String? {
            for (ext in this) {
                val url = ext.queryImageUrl(bot, image) ?: continue
                return url
            }
            return null
        }
        fun List<MediaURLService>.queryAudioUrl(audio: Audio): String? {
            for (ext in this) {
                val url = ext.queryAudioUrl(audio) ?: continue
                return url
            }
            return null
        }
        fun List<MediaURLService>.queryVideoUrl(video: ShortVideo): String? {
            for (ext in this) {
                val url = ext.queryVideoUrl(video) ?: continue
                return url
            }
            return null
        }
    }
}

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.ExternalResource
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.spi.FileService
import kotlin.coroutines.CoroutineContext

internal class OtherClientWrapper(
    override val bot: BotWrapper,
    override var info: OtherClientInfo
) : OtherClient {
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${bot.id})OtherClient/${info.deviceKind}")

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo {
        return OnebotMessages.videoFromFile(FileService.instance!!.upload(video))
    }

}
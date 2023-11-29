package top.mrxiaom.overflow.internal.contact

import cn.evole.onebot.sdk.response.misc.ClientsResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.message.data.WrappedVideo
import top.mrxiaom.overflow.internal.utils.ResourceUtils.toBase64File
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
class OtherClientWrapper(
    val botWrapper: BotWrapper,
    internal var impl: ClientsResp.Clients
) : OtherClient {
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${bot.id})OtherClient/${impl.deviceKind}")
    val data: ClientsResp.Clients
        get() = impl

    override val bot: Bot
        get() = botWrapper
    override val info: OtherClientInfo
        get() = OtherClientInfo(impl.appId.toInt(), null, impl.deviceName, impl.deviceKind)

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo {
        return WrappedVideo(video.toBase64File())
    }

}
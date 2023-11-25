package top.mrxiaom.overflow.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext
import cn.evole.onebot.sdk.response.group.GroupInfoResp;
import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import cn.evole.onebot.sdk.response.misc.ClientsResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.Group.Companion.setEssenceMessage
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.message.OnebotMessages
import top.mrxiaom.overflow.message.data.WrappedAudio
import top.mrxiaom.overflow.message.data.WrappedVideo
import top.mrxiaom.overflow.utils.ResourceUtils.toBase64File
import java.util.Base64

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
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

class OtherClientWrapper(
    val botWrapper: BotWrapper,
    private var impl: ClientsResp.Clients
) : OtherClient {

    val data: ClientsResp.Clients
        get() = impl
    suspend fun queryUpdate() {
        //impl = botWrapper.impl.getOnlineClients(false).data.clients.filter {  }
    }

    override val bot: Bot
        get() = TODO("Not yet implemented")
    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
    override val info: OtherClientInfo
        get() = TODO("Not yet implemented")

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo {
        TODO("Not yet implemented")
    }

}
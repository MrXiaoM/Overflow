package top.mrxiaom.overflow.contact

import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evole.onebot.sdk.response.group.GroupInfoResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.ExternalResource
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.message.OnebotMessages
import top.mrxiaom.overflow.message.data.WrappedAudio
import top.mrxiaom.overflow.message.data.WrappedVideo
import top.mrxiaom.overflow.utils.ResourceUtils.toBase64File
import kotlin.coroutines.CoroutineContext

class StrangerWrapper(
    val botWrapper: BotWrapper,
    private var impl: FriendInfoResp,
) : Stranger {

    override val bot: Bot = botWrapper
    override val id: Long = impl.userId
    override val nick: String = impl.nickname
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${bot.id})Stranger/$id")
    override var remark: String
        get() = impl.remark
        set(value) {
            Overflow.logger.warning("Onebot 未提供修改陌生人备注接口 ($id)")
        }

    override suspend fun delete() {
        Overflow.logger.warning("Onebot 未提供删除陌生人接口 ($id)")
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        val msg = OnebotMessages.serializeToOneBotJson(message)
        val response = botWrapper.impl.sendPrivateMsg(id, msg, false)
        val messageId = response.data.messageId
        TODO("MessageReceipt")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        return Image.fromId(resource.toBase64File())
    }

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo {
        return WrappedVideo(video.toBase64File())
    }
}
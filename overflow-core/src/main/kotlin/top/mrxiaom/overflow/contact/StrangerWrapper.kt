package top.mrxiaom.overflow.contact

import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evole.onebot.sdk.response.group.GroupInfoResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.currentTimeSeconds
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.message.OnebotMessages
import top.mrxiaom.overflow.message.OnebotMessages.findForwardMessage
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

    @OptIn(MiraiInternalApi::class)
    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        val forward = message.findForwardMessage()
        val messageId = if (forward != null) {
            val nodes = OnebotMessages.serializeForwardNodes(forward.nodeList)
            val response = botWrapper.impl.sendPrivateForwardMsg(id, nodes)
            response.data.messageId
        } else {
            val msg = OnebotMessages.serializeToOneBotJson(message)
            val response = botWrapper.impl.sendPrivateMsg(id, msg, false)
            response.data.messageId
        }
        @Suppress("DEPRECATION_ERROR")
        return MessageReceipt(object : OnlineMessageSource.Outgoing.ToStranger(){
            override val bot: Bot = this@StrangerWrapper.bot
            override val ids: IntArray = IntArray(messageId)
            override val internalIds: IntArray = ids
            override val isOriginalMessageInitialized: Boolean = true
            override val originalMessage: MessageChain = message.toMessageChain()
            override val sender: Bot = bot
            override val target: Stranger = this@StrangerWrapper
            override val time: Int = currentTimeSeconds().toInt()
        }, this)
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
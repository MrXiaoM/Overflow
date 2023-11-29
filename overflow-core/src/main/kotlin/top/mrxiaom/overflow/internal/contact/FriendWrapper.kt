package top.mrxiaom.overflow.internal.contact

import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.FriendMessagePostSendEvent
import net.mamoe.mirai.event.events.FriendMessagePreSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.currentTimeSeconds
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.OnebotMessages.findForwardMessage
import top.mrxiaom.overflow.internal.message.data.WrappedAudio
import top.mrxiaom.overflow.internal.message.data.WrappedVideo
import top.mrxiaom.overflow.internal.utils.ResourceUtils.toBase64File
import kotlin.coroutines.CoroutineContext

class FriendWrapper(
    val botWrapper: BotWrapper,
    internal var impl: FriendInfoResp,
) : Friend {

    override val bot: Bot = botWrapper
    override val id: Long = impl.userId
    override val nick: String = impl.nickname
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${bot.id})Friend/$id")
    override val friendGroup: FriendGroup
        get() = throw NotImplementedError("Onebot 未提供好友分组接口")
    override var remark: String
        get() = impl.remark
        set(_) {
            Overflow.logger.warning("Onebot 未提供修改好友备注接口 ($id)")
        }
    override val roamingMessages: RoamingMessages
        get() = throw NotImplementedError("Onebot 未提供消息漫游接口")

    override suspend fun delete() {
        botWrapper.impl.deleteFriend(id)
    }

    @OptIn(MiraiInternalApi::class)
    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        if (FriendMessagePreSendEvent(this, message).broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")

        val messageChain = message.toMessageChain()
        var throwable: Throwable? = null
        val receipt = kotlin.runCatching {
            val forward = messageChain.findForwardMessage()
            val messageId = if (forward != null) {
                val nodes = OnebotMessages.serializeForwardNodes(forward.nodeList)
                val response = botWrapper.impl.sendPrivateForwardMsg(id, nodes)
                response.data.messageId
            } else {
                val msg = OnebotMessages.serializeToOneBotJson(messageChain)
                val response = botWrapper.impl.sendPrivateMsg(id, msg, false)
                response.data.messageId
            }

            @Suppress("DEPRECATION_ERROR")
            MessageReceipt(object : OnlineMessageSource.Outgoing.ToFriend() {
                override val bot: Bot = this@FriendWrapper.bot
                override val ids: IntArray = arrayOf(messageId).toIntArray()
                override val internalIds: IntArray = ids
                override val isOriginalMessageInitialized: Boolean = true
                override val originalMessage: MessageChain = messageChain
                override val sender: Bot = bot
                override val target: Friend = this@FriendWrapper
                override val time: Int = currentTimeSeconds().toInt()
            }, this)
        }.onFailure { throwable = it }.getOrNull()
        FriendMessagePostSendEvent(this, messageChain, throwable, receipt).broadcast()

        return receipt ?: throw throwable!!
    }

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio {
        return WrappedAudio(resource.toBase64File())
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
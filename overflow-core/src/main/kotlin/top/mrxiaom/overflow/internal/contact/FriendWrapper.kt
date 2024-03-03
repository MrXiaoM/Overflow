@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import kotlinx.coroutines.CoroutineName
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
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.OnebotMessages.findForwardMessage
import top.mrxiaom.overflow.internal.message.data.OutgoingSource
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.receipt
import top.mrxiaom.overflow.internal.utils.safeMessageIds
import top.mrxiaom.overflow.spi.FileService
import kotlin.coroutines.CoroutineContext

internal class FriendWrapper(
    override val bot: BotWrapper,
    internal var impl: FriendInfoResp,
) : Friend {
    override val id: Long = impl.userId
    override val nick: String = impl.nickname
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${bot.id})Friend/$id")
    override val friendGroup: FriendGroup = bot.friendGroups.fallbackFriendGroup
    override var remark: String
        get() = impl.remark
        set(_) {
            OverflowAPI.logger.warning("Onebot 未提供修改好友备注接口 ($id)")
        }
    override val roamingMessages: RoamingMessages
        get() = throw NotImplementedError("Onebot 未提供消息漫游接口")

    override suspend fun delete() {
        bot.impl.deleteFriend(id)
    }

    @OptIn(MiraiInternalApi::class)
    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        if (FriendMessagePreSendEvent(this, message).broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")

        val messageChain = message.toMessageChain()
        var throwable: Throwable? = null
        val receipt = runCatching {
            val forward = messageChain.findForwardMessage()
            val messageIds = if (forward != null) {
                OnebotMessages.sendForwardMessage(this, forward).safeMessageIds
            } else {
                val msg = Overflow.serializeMessage(bot, messageChain)
                val response = bot.impl.sendPrivateMsg(id, msg, false)
                response.data.safeMessageIds
            }

            OutgoingSource.friend(
                bot = bot,
                ids = messageIds,
                internalIds = messageIds,
                isOriginalMessageInitialized = true,
                originalMessage = messageChain,
                sender = bot,
                target = this@FriendWrapper,
                time = currentTimeSeconds().toInt()
            ).receipt(this)
        }.onFailure { throwable = it }.getOrNull()
        FriendMessagePostSendEvent(this, messageChain, throwable, receipt).broadcast()

        bot.logger.verbose("Friend($id) <- $messageChain")

        return receipt ?: throw throwable!!
    }

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio {
        return OnebotMessages.audioFromFile(FileService.instance!!.upload(resource)) as OfflineAudio
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        return OnebotMessages.imageFromFile(FileService.instance!!.upload(resource))
    }

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo {
        return OnebotMessages.videoFromFile(FileService.instance!!.upload(video))
    }

    override fun toString(): String = "Friend($id)"
}
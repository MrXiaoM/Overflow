@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import cn.evole.onebot.sdk.response.contact.StrangerInfoResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.StrangerMessagePostSendEvent
import net.mamoe.mirai.event.events.StrangerMessagePreSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.message.data.toMessageChain
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

internal class StrangerWrapper(
    override val bot: BotWrapper,
    internal var impl: StrangerInfoResp,
) : Stranger {
    override val id: Long = impl.userId
    override val nick: String = impl.nickname
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${bot.id})Stranger/$id")
    override var remark: String
        get() = ""
        set(_) {
            OverflowAPI.logger.warning("Onebot 未提供修改陌生人备注接口 ($id)")
        }

    override suspend fun delete() {
        OverflowAPI.logger.warning("Onebot 未提供删除陌生人接口 ($id)")
    }

    @OptIn(MiraiInternalApi::class)
    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        if (StrangerMessagePreSendEvent(this, message).broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")

        val messageChain = message.toMessageChain()
        var throwable: Throwable? = null
        val receipt = kotlin.runCatching {
            val forward = message.findForwardMessage()
            val messageIds = if (forward != null) {
                OnebotMessages.sendForwardMessage(this, forward).safeMessageIds
            } else {
                val msg = Overflow.serializeMessage(bot, message)
                val response = bot.impl.sendPrivateMsg(id, msg, false)
                response.data.safeMessageIds
            }

            return OutgoingSource.stranger(
                bot = bot,
                ids = messageIds,
                internalIds = messageIds,
                isOriginalMessageInitialized = true,
                originalMessage = message.toMessageChain(),
                sender = bot,
                target = this,
                time = currentTimeSeconds().toInt()
            ).receipt(this)
        }.onFailure { throwable = it }.getOrNull()
        StrangerMessagePostSendEvent(this, messageChain, throwable, receipt).broadcast()

        bot.logger.verbose("Stranger($id) <- $messageChain")

        return receipt ?: throw throwable!!
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        return Overflow.imageFromFile(FileService.instance!!.upload(resource))
    }

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo {
        return OnebotMessages.videoFromFile(FileService.instance!!.upload(video))
    }

    override fun toString(): String = "Stranger($id)"
}
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.response.contact.StrangerInfoResp
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.AvatarSpec
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
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.contact.RemoteUser
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.receipt
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.strangerMsg
import top.mrxiaom.overflow.spi.FileService
import kotlin.coroutines.CoroutineContext

internal class StrangerWrapper(
    override val bot: BotWrapper,
    internal var impl: StrangerInfoResp,
    internal var implJson: JsonElement,
) : Stranger, RemoteUser, CanSendMessage {
    override val onebotData: String
        get() = gson.toJson(implJson)
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

    private val avatar: String? by lazy {
        if (bot.appName.lowercase() != "gensokyo") null
        else runBlocking { bot.impl.extGetAvatar(null, id).data }
    }
    override fun avatarUrl(spec: AvatarSpec): String {
        return avatar ?: super.avatarUrl(spec)
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        if (StrangerMessagePreSendEvent(this, message).broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")

        val messageChain = message.toMessageChain()
        val (messageIds, throwable) = bot.sendMessageCommon(this, messageChain)
        val receipt = strangerMsg(messageIds, messageChain).receipt(this)
        StrangerMessagePostSendEvent(
            target = this,
            message = messageChain,
            exception = throwable,
            receipt = receipt.takeIf { throwable == null }
        ).broadcast()

        bot.logger.verbose("Stranger($id) <- $messageChain")

        return receipt
    }

    override suspend fun sendToOnebot(message: String): MsgId? {
        val resp = bot.impl.sendPrivateMsg(id, null, message, false) {
            throwExceptions(true)
        }
        return resp.data
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

    override fun toString(): String = "Stranger($id)"
}

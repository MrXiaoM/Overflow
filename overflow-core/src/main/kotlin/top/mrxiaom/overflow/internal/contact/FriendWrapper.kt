@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.response.contact.FriendInfoResp
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.AvatarSpec
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.FriendMessagePostSendEvent
import net.mamoe.mirai.event.events.FriendMessagePreSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.spi.AudioToSilkService
import net.mamoe.mirai.utils.ExternalResource
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.contact.RemoteUser
import top.mrxiaom.overflow.internal.data.UserProfileImpl
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.friendMsg
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.receipt
import top.mrxiaom.overflow.spi.FileService
import kotlin.coroutines.CoroutineContext

internal class FriendWrapper(
    override val bot: BotWrapper,
    internal var impl: FriendInfoResp,
    internal var implJson: JsonElement,
) : Friend, RemoteUser, CanSendMessage {
    override val onebotData: String
        get() = gson.toJson(implJson)
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

    private val avatar: String? by lazy {
        if (bot.appName.lowercase() != "gensokyo") null
        else runBlocking { bot.impl.extGetAvatar(null, id).data }
    }
    override fun avatarUrl(spec: AvatarSpec): String {
        return avatar ?: super.avatarUrl(spec)
    }

    override suspend fun queryProfile(): UserProfile {
        val reference = super.queryProfile()
        return UserProfileImpl(
            age = Math.max(reference.age, impl.age),
            email = impl.email.takeIf { it.isNotEmpty() } ?: reference.email,
            friendGroupId = reference.friendGroupId,
            nickname = nick,
            qLevel = Math.max(reference.qLevel, impl.level),
            sex = reference.sex,
            sign = reference.sign
        )
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        val event = FriendMessagePreSendEvent(this, message)
        if (event.broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")

        val messageChain = event.message.toMessageChain()
        val (messageIds, throwable) = bot.sendMessageCommon(this, messageChain)
        val receipt = friendMsg(messageIds, messageChain).receipt(this)
        FriendMessagePostSendEvent(
            target = this,
            message = messageChain,
            exception = throwable,
            receipt = receipt.takeIf { throwable == null }
        ).broadcast()

        bot.logger.verbose("Friend($id) <- $messageChain")

        return receipt
    }

    override suspend fun sendToOnebot(message: String): MsgId? {
        val resp = bot.impl.sendPrivateMsg(id, null, message, false) {
            throwExceptions(true)
        }
        return resp.data
    }

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio {
        val res = AudioToSilkService.instance.convert(resource)
        return OnebotMessages.audioFromFile(FileService.instance!!.upload(res)) as OfflineAudio
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

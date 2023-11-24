package top.mrxiaom.overflow.contact

import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.ExternalResource
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.message.OnebotMessages
import top.mrxiaom.overflow.message.data.WrappedVideo
import top.mrxiaom.overflow.utils.ResourceUtils.toBase64File
import kotlin.coroutines.CoroutineContext

class MemberWrapper(
    val botWrapper: BotWrapper,
    val groupWrapper: GroupWrapper,
    private var impl: GroupMemberInfoResp
) : NormalMember {

    val data: GroupMemberInfoResp
        get() = impl
    suspend fun queryUpdate() {
        impl = botWrapper.impl.getGroupMemberInfo(impl.groupId, impl.userId, false).data
    }

    override val active: MemberActive
        get() = TODO("Not yet implemented")
    override val bot: Bot = botWrapper
    override val group: Group = groupWrapper
    override val id: Long = impl.userId
    override val joinTimestamp: Int
        get() = impl.joinTime
    override val lastSpeakTimestamp: Int
        get() = impl.lastSentTime
    override val muteTimeRemaining: Int
        get() = TODO("Not yet implemented")
    override val coroutineContext: CoroutineContext = CoroutineName("((Bot/${bot.id})Group/${group.id})Member/$id")
    override var nameCard: String
        get() = impl.card
        set(value) {
            Overflow.instance.scope.launch {
                botWrapper.impl.setGroupCard(impl.groupId, id, value)
            }
        }
    override val nick: String = impl.nickname
    override val permission: MemberPermission = when(impl.role) {
        "owner" -> MemberPermission.OWNER
        "admin" -> MemberPermission.ADMINISTRATOR
        else -> MemberPermission.MEMBER
    }
    override val remark: String
        get() = botWrapper.friends[id]?.remark ?: ""
    override var specialTitle: String
        get() = impl.title
        set(value) {
            Overflow.instance.scope.launch {
                botWrapper.impl.setGroupSpecialTitle(impl.groupId, id, value, -1)
            }
        }
    override suspend fun kick(message: String, block: Boolean) {
        botWrapper.impl.setGroupKick(impl.groupId, id, block)
    }

    override suspend fun modifyAdmin(operation: Boolean) {
        botWrapper.impl.setGroupAdmin(impl.groupId, id, operation)
    }

    override suspend fun mute(durationSeconds: Int) {
        botWrapper.impl.setGroupBan(group.id, id, durationSeconds)
    }

    override fun nudge(): MemberNudge {
        return MemberNudge(this)
    }

    override suspend fun sendMessage(message: String): MessageReceipt<NormalMember> {
        return sendMessage(PlainText(message))
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<NormalMember> {
        val msg = OnebotMessages.serializeToOneBotJson(message)
        val response = botWrapper.impl.sendPrivateMsg(id, msg, false)
        val messageId = response.data.messageId
        TODO("MessageReceipt")
    }

    override suspend fun unmute() {
        mute(0)
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
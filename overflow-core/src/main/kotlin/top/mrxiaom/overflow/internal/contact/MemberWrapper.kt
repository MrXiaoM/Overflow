@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.currentTimeSeconds
import top.mrxiaom.overflow.contact.Updatable
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.check
import top.mrxiaom.overflow.internal.contact.data.MemberActiveWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.OnebotMessages.findForwardMessage
import top.mrxiaom.overflow.internal.message.data.WrappedVideo
import top.mrxiaom.overflow.internal.utils.safeMessageIds
import top.mrxiaom.overflow.spi.FileService
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
class MemberWrapper(
    val botWrapper: BotWrapper,
    val groupWrapper: GroupWrapper,
    internal var impl: GroupMemberInfoResp
) : NormalMember, Updatable {
    val data: GroupMemberInfoResp
        get() = impl
    override suspend fun queryUpdate() {
        setImpl(botWrapper.impl.getGroupMemberInfo(impl.groupId, impl.userId, false).data)
    }
    
    fun setImpl(impl: GroupMemberInfoResp) {
        if (impl.card != impl.card) {
            botWrapper.eventDispatcher.broadcastAsync(
                MemberCardChangeEvent(this.impl.card, impl.card, this)
            )
        }
        this.impl = impl
    }

    override val active: MemberActiveWrapper = MemberActiveWrapper(this)
    override val bot: Bot = botWrapper
    override val group: Group = groupWrapper
    override val id: Long = impl.userId
    override val joinTimestamp: Int
        get() = impl.joinTime
    override val lastSpeakTimestamp: Int
        get() = impl.lastSentTime
    override val muteTimeRemaining: Int
        get() = (impl.shutUpTimestamp - currentTimeSeconds()).takeIf { it > 0 }?.toInt() ?: 0

    override val coroutineContext: CoroutineContext = CoroutineName("((Bot/${bot.id})Group/${group.id})Member/$id")
    override var nameCard: String
        get() = impl.card ?: ""
        set(value) {
            if (id != bot.id) {
                group.checkBotPermission(MemberPermission.ADMINISTRATOR)
            }
            Overflow.instance.launch {
                botWrapper.impl.setGroupCard(impl.groupId, id, value)
            }
        }
    override val nick: String
        get() = impl.nickname
    override val permission: MemberPermission
        get() = when(impl.role) {
            "owner" -> MemberPermission.OWNER
            "admin" -> MemberPermission.ADMINISTRATOR
            else -> MemberPermission.MEMBER
        }
    override val remark: String
        get() = botWrapper.friends[id]?.remark ?: ""
    override var specialTitle: String
        get() = impl.title
        set(value) {
            Overflow.instance.launch {
                botWrapper.impl.setGroupSpecialTitle(impl.groupId, id, value, -1)
            }
        }
    override suspend fun kick(message: String, block: Boolean) {
        checkBotPermissionHigherThanThis("移出本群")
        check(group.members[this.id] != null) {
            "群成员 ${this.id} 已经被踢出群 ${group.id} 了."
        }
        if (botWrapper.impl.setGroupKick(impl.groupId, id, block)
            .check("将 $id 移出群聊 ${group.id}")) {
            groupWrapper.members.remove(id)
        }
    }

    override suspend fun modifyAdmin(operation: Boolean) {
        checkBotPermissionHighest("设置管理员")
        if (botWrapper.impl.setGroupAdmin(impl.groupId, id, operation)
            .check("设置 $id 在群聊 ${group.id} 的管理员状态为 $operation")) {
            impl.role = if (operation) "admin" else "member"
        }
    }

    override suspend fun mute(durationSeconds: Int) {
        check(this.id != bot.id) {
            "机器人不能禁言自己."
        }
        require(durationSeconds > 0) {
            "durationSeconds 必须要大于0."
        }
        checkBotPermissionHigherThanThis("禁言")
        if (botWrapper.impl.setGroupBan(group.id, id, durationSeconds)
            .check("禁言群成员 $id")) {
            groupWrapper.updateMember(id)
        }
    }

    override suspend fun unmute() {
        checkBotPermissionHigherThanThis("解除禁言")
        if (botWrapper.impl.setGroupBan(group.id, id, 0)
            .check("解除禁言群成员 $id")) {
            groupWrapper.updateMember(id)
        }
    }

    override fun nudge(): MemberNudge {
        return MemberNudge(this)
    }

    override suspend fun sendMessage(message: String): MessageReceipt<NormalMember> {
        return sendMessage(PlainText(message))
    }

    @OptIn(MiraiInternalApi::class)
    override suspend fun sendMessage(message: Message): MessageReceipt<NormalMember> {
        if (GroupTempMessagePreSendEvent(this, message).broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")

        val messageChain = message.toMessageChain()
        var throwable: Throwable? = null
        val receipt = kotlin.runCatching {
            val forward = message.findForwardMessage()
            val messageIds = if (forward != null) {
                val nodes = OnebotMessages.serializeForwardNodes(forward.nodeList)
                val response = botWrapper.impl.sendPrivateForwardMsg(id, nodes)
                response.data.safeMessageIds
            } else {
                val msg = OnebotMessages.serializeToOneBotJson(message)
                val response = botWrapper.impl.sendPrivateMsg(id, msg, false)
                response.data.safeMessageIds
            }
            @Suppress("DEPRECATION_ERROR")
            MessageReceipt(object : OnlineMessageSource.Outgoing.ToTemp() {
                override val bot: Bot = this@MemberWrapper.bot
                override val ids: IntArray = messageIds
                override val internalIds: IntArray = ids
                override val isOriginalMessageInitialized: Boolean = true
                override val originalMessage: MessageChain = message.toMessageChain()
                override val sender: Bot = bot
                override val target: Member = this@MemberWrapper
                override val time: Int = currentTimeSeconds().toInt()
            }, this)
        }.onFailure { throwable = it }.getOrNull()
        GroupTempMessagePostSendEvent(this, messageChain, throwable, receipt).broadcast()

        bot.logger.verbose("Member(${group.id}:$id) <- $messageChain")

        return receipt ?: throw throwable!!
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

    override fun toString(): String = "NormalMember($id)"
}

internal fun Member.checkBotPermissionHighest(operationName: String) {
    check(group.botPermission == MemberPermission.OWNER) {
        throw PermissionDeniedException(
            "对群成员 $id 的操作 `$operationName` 需要 群主 权限, 但机器人在群 ${group.id} 内仅有 ${group.botPermission.display} 权限.",
        )
    }
}

internal fun Member.checkBotPermissionHigherThanThis(operationName: String) {
    check(group.botPermission > this.permission) {
        throw PermissionDeniedException(
            "对群 ${group.id} 的群成员 $id 的操作 `$operationName` 需要机器人比该成员拥有更高的权限, 但目前 " +
                    "${group.botPermission.display} < ${this.permission.display}.",
        )
    }
}

internal val MemberPermission.display: String
    get() = when(this) {
        MemberPermission.OWNER -> "群主"
        MemberPermission.ADMINISTRATOR -> "管理员"
        MemberPermission.MEMBER -> "群员"
    }

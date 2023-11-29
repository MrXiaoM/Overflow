package top.mrxiaom.overflow.internal.contact

import cn.evole.onebot.sdk.response.group.GroupInfoResp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent
import net.mamoe.mirai.event.events.GroupMessagePreSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.currentTimeSeconds
import top.mrxiaom.overflow.internal.contact.data.AnnouncementsWrapper
import top.mrxiaom.overflow.internal.contact.data.AnnouncementsWrapper.Companion.fetchAnnouncements
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.OnebotMessages.findForwardMessage
import top.mrxiaom.overflow.internal.message.data.WrappedAudio
import top.mrxiaom.overflow.internal.message.data.WrappedVideo
import top.mrxiaom.overflow.internal.utils.ResourceUtils.toBase64File
import top.mrxiaom.overflow.internal.utils.update
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
class GroupWrapper(
    val botWrapper: BotWrapper,
    internal var impl: GroupInfoResp
) : Group {
    private var membersInternal: ContactList<MemberWrapper>? = null
    private var announcementsInternal: AnnouncementsWrapper? = null

    val data: GroupInfoResp
        get() = impl
    suspend fun queryUpdate() {
        impl = botWrapper.impl.getGroupInfo(impl.groupId, false).data
    }
    suspend fun updateAnnouncements() {
        announcementsInternal = fetchAnnouncements()
    }
    internal fun updateMember(member: MemberWrapper): MemberWrapper {
        return ((members[member.id] as? MemberWrapper) ?: member.also { members.delegate.add(it) }).apply {
            impl = member.impl
        }
    }

    override val bot: Bot
        get() = botWrapper
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${botWrapper.id})Group/$id")
    override val active: GroupActive
        get() {
            //val resp = botWrapper.impl.getGroupHonorInfo(id, "all").data
            TODO("Not yet implemented")
        }
    override val announcements: Announcements
        get() = announcementsInternal ?: runBlocking {
            fetchAnnouncements().also {
                announcementsInternal = it
            }
        }
    override val essences: Essences
        get() {
            //for (resp in botWrapper.impl.getEssenceMsgList(id).data) {
            //
            //}
            TODO("Not yet implemented")
        }
    override val files: RemoteFiles
        get() {
            //val resp = botWrapper.impl.getGroupRootFiles(id).data
            TODO("Not yet implemented")
        }
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(
        "Please use files instead.",
        replaceWith = ReplaceWith("files.root"),
        level = DeprecationLevel.ERROR
    ) // deprecated since 2.8.0-RC
    @DeprecatedSinceMirai(warningSince = "2.8", errorSince = "2.14")
    public override val filesRoot: net.mamoe.mirai.utils.RemoteFile
        get() = throw IllegalStateException("Deprecated")

    override val id: Long
        get() = impl.groupId
    override var name: String
        get() = impl.groupName
        set(value) { impl.groupName = value }
    override val members: ContactList<NormalMember>
        get() = membersInternal ?: runBlocking {
            ContactList<MemberWrapper>().apply {
                val data = botWrapper.impl.getGroupMemberList(id).data ?: return@apply
                update(data.map {
                    MemberWrapper(botWrapper, this@GroupWrapper, it)
                }) { impl = it.impl }
                membersInternal = this
            }
        }
    override val botAsMember: NormalMember
        get() = members.first { it.id == bot.id }
    override val owner: NormalMember
        get() = members.first { it.permission == MemberPermission.OWNER }
    override val roamingMessages: RoamingMessages
        get() = throw NotImplementedError("Onebot 未提供消息漫游接口")
    override val settings: GroupSettings
        get() = TODO("Not yet implemented")

    override fun contains(id: Long): Boolean {
        return members.contains(id)
    }

    override fun get(id: Long): NormalMember? {
        return members[id]
    }

    override suspend fun quit(): Boolean {
        if (botAsMember.isOwner()) {
            throw IllegalStateException("机器人是群主，无法退群")
        }
        botWrapper.impl.setGroupLeave(id, false)
        return true
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        if (GroupMessagePreSendEvent(this, message).broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")

        val messageChain = message.toMessageChain()
        var throwable: Throwable? = null
        val receipt = kotlin.runCatching {
            val forward = messageChain.findForwardMessage()
            val messageId = if (forward != null) {
                val nodes = OnebotMessages.serializeForwardNodes(forward.nodeList)
                val response = botWrapper.impl.sendGroupForwardMsg(id, nodes)
                response.data.messageId
            } else {
                val msg = OnebotMessages.serializeToOneBotJson(messageChain)
                val response = botWrapper.impl.sendGroupMsg(id, msg, false)
                response.data.messageId
            }
            @Suppress("DEPRECATION_ERROR")
            MessageReceipt(object : OnlineMessageSource.Outgoing.ToGroup() {
                override val bot: Bot = this@GroupWrapper.bot
                override val ids: IntArray = arrayOf(messageId).toIntArray()
                override val internalIds: IntArray = ids
                override val isOriginalMessageInitialized: Boolean = true
                override val originalMessage: MessageChain = messageChain
                override val sender: Bot = bot
                override val target: Group = this@GroupWrapper
                override val time: Int = currentTimeSeconds().toInt()
            }, this)
        }.onFailure { throwable = it }.getOrNull()
        GroupMessagePostSendEvent(this, messageChain, throwable, receipt).broadcast()

        return receipt ?: throw throwable!!
    }

    override suspend fun setEssenceMessage(source: MessageSource): Boolean {
        // TODO 权限
        botWrapper.impl.setEssenceMsg(source.ids[0])
        return true
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

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(
        "use uploadAudio",
        replaceWith = ReplaceWith("uploadAudio(resource)"),
        level = DeprecationLevel.HIDDEN
    )
    override suspend fun uploadVoice(resource: ExternalResource): net.mamoe.mirai.message.data.Voice {
        throw IllegalStateException("Deprecated")
    }
}
package top.mrxiaom.overflow.contact

import cn.evole.onebot.sdk.response.group.GroupInfoResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.currentTimeSeconds
import top.mrxiaom.overflow.message.OnebotMessages
import top.mrxiaom.overflow.message.OnebotMessages.findForwardMessage
import top.mrxiaom.overflow.message.data.WrappedAudio
import top.mrxiaom.overflow.message.data.WrappedVideo
import top.mrxiaom.overflow.utils.ResourceUtils.toBase64File
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
class GroupWrapper(
    val botWrapper: BotWrapper,
    private var impl: GroupInfoResp
) : Group {
    private var membersInternal: ContactList<MemberWrapper> = ContactList()
    val data: GroupInfoResp
        get() = impl
    suspend fun queryUpdate() {
        impl = botWrapper.impl.getGroupInfo(impl.groupId, false).data
        membersInternal = ContactList(botWrapper.impl.getGroupMemberList(id).data.map {
            MemberWrapper(botWrapper, this@GroupWrapper, it)
        }.toMutableList())
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
        get() = TODO("Not yet implemented")
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
        get() = membersInternal
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
        val forward = message.findForwardMessage()
        val messageId = if (forward != null) {
            val nodes = OnebotMessages.serializeForwardNodes(forward.nodeList)
            val response = botWrapper.impl.sendGroupForwardMsg(id, nodes)
            response.data.messageId
        } else {
            val msg = OnebotMessages.serializeToOneBotJson(message)
            val response = botWrapper.impl.sendGroupMsg(id, msg, false)
            response.data.messageId
        }
        @Suppress("DEPRECATION_ERROR")
        return MessageReceipt(object : OnlineMessageSource.Outgoing.ToGroup(){
            override val bot: Bot = this@GroupWrapper.bot
            override val ids: IntArray = arrayOf(messageId).toIntArray()
            override val internalIds: IntArray = ids
            override val isOriginalMessageInitialized: Boolean = true
            override val originalMessage: MessageChain = message.toMessageChain()
            override val sender: Bot = bot
            override val target: Group = this@GroupWrapper
            override val time: Int = currentTimeSeconds().toInt()
        }, this)
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
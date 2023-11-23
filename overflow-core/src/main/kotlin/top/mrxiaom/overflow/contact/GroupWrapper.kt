package top.mrxiaom.overflow.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext
import cn.evole.onebot.sdk.response.group.GroupInfoResp;
import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import kotlinx.coroutines.CoroutineName
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.message.OnebotMessages
import top.mrxiaom.overflow.message.data.WrappedAudio
import top.mrxiaom.overflow.message.data.WrappedVideo
import top.mrxiaom.overflow.utils.ResourceUtils.toBase64File
import java.util.Base64

class GroupWrapper(
    val botWrapper: BotWrapper,
    private var impl: GroupInfoResp
) : Group {

    val data: GroupInfoResp
        get() = impl
    fun queryUpdate() {
        impl = botWrapper.impl.getGroupInfo(impl.groupId, false).data
    }

    override val bot: Bot
        get() = botWrapper
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${botWrapper.id})Group/$id")
    override val active: GroupActive
        get() = TODO("Not yet implemented")
    override val announcements: Announcements
        get() = TODO("Not yet implemented")
    override val essences: Essences
        get() = TODO("Not yet implemented")
    override val files: RemoteFiles
        get() = TODO("Not yet implemented")
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
    @OptIn(MiraiInternalApi::class)
    override val members: ContactList<NormalMember>
        get() {
            return ContactList(botWrapper.impl.getGroupMemberList(id).data.map {
                MemberWrapper(botWrapper, this@GroupWrapper, it)
            }.toMutableList())
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
        throw NotImplementedError("Onebot 未提供退群接口")
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        val msg = OnebotMessages.serializeToOneBotJson(message)
        val response = botWrapper.impl.sendGroupMsg(id, msg, false)
        val messageId = response.data.messageId
        TODO("MessageReceipt")
    }

    override suspend fun setEssenceMessage(source: MessageSource): Boolean {
        TODO("Not yet implemented")
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
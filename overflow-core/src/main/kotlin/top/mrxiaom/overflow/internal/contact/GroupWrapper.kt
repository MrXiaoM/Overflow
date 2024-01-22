@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import cn.evole.onebot.sdk.response.group.GroupInfoResp
import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.announcement.Announcements
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
import top.mrxiaom.overflow.contact.RemoteGroup
import top.mrxiaom.overflow.contact.Updatable
import top.mrxiaom.overflow.internal.contact.data.*
import top.mrxiaom.overflow.internal.contact.data.AnnouncementsWrapper.Companion.fetchAnnouncements
import top.mrxiaom.overflow.internal.contact.data.EssencesWrapper.Companion.fetchEssences
import top.mrxiaom.overflow.internal.contact.data.RemoteFilesWrapper.Companion.fetchFiles
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.OnebotMessages.findForwardMessage
import top.mrxiaom.overflow.internal.message.data.OutgoingSource
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.receipt
import top.mrxiaom.overflow.internal.utils.safeMessageIds
import top.mrxiaom.overflow.internal.utils.update
import top.mrxiaom.overflow.internal.utils.wrapAsMember
import top.mrxiaom.overflow.spi.FileService
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
internal class GroupWrapper(
    val botWrapper: BotWrapper,
    internal var impl: GroupInfoResp
) : Group, RemoteGroup, Updatable {
    private var membersInternal: ContactList<MemberWrapper>? = null

    val data: GroupInfoResp
        get() = impl
    override suspend fun queryUpdate() {
        impl = botWrapper.impl.getGroupInfo(impl.groupId, false).data
    }

    override suspend fun updateAnnouncements(): Announcements {
        return announcements.also { it.update() }
    }
    internal suspend fun updateMember(userId: Long): MemberWrapper? {
        return botWrapper.impl.getGroupMemberInfo(id, userId, false).data?.run { updateMember(this) }
    }
    internal fun updateMember(member: GroupMemberInfoResp): MemberWrapper {
        return (members[member.userId] ?: MemberWrapper(botWrapper, this, member).also { members.delegate.add(it) }).apply {
            impl = member
        }
    }

    internal suspend fun queryMember(userId: Long): MemberWrapper? {
        if (userId == bot.id) return botAsMember
        return members[userId] ?: botWrapper.impl
            .getGroupMemberInfo(id, userId, false).data?.wrapAsMember(this)
    }

    @JvmBlockingBridge
    override suspend fun updateGroupMemberList(): ContactList<MemberWrapper> {
        return (membersInternal ?: ContactList()).apply {
            val data = botWrapper.impl.getGroupMemberList(id).data
            update(data?.map {
                MemberWrapper(botWrapper, this@GroupWrapper, it)
            }) { setImpl(it.impl) }
            membersInternal = this
        }
    }

    override val bot: BotWrapper
        get() = botWrapper
    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${botWrapper.id})Group/$id")

    /**
     * 几乎全是 http api，但是也挺多的，有空再写
     * https://github.com/mamoe/mirai/blob/dev/mirai-core/src/commonMain/kotlin/contact/active/GroupActiveProtocol.kt
     */
    override val active: GroupActiveWrapper by lazy {
        runBlocking {
            GroupActiveWrapper(
                this@GroupWrapper
            ).apply {
                runCatching {
                    refresh()
                }.onFailure {
                    bot.logger.warning(it)
                }
            }
        }
    }
    override val announcements: AnnouncementsWrapper by lazy {
        runBlocking { fetchAnnouncements() }
    }
    override val essences: EssencesWrapper by lazy {
        runBlocking { fetchEssences() }
    }
    override val files: RemoteFilesWrapper by lazy {
        runBlocking { fetchFiles() }
    }
    override val id: Long
        get() = impl.groupId
    override var name: String
        get() = impl.groupName
        set(value) {
            checkBotPermission(MemberPermission.ADMINISTRATOR)
            runBlocking {
                botWrapper.impl.setGroupName(id, value)
                impl.groupName = value
            }
        }
    override val members: ContactList<MemberWrapper>
        get() = membersInternal ?: runBlocking {
            updateGroupMemberList()
        }
    override val botAsMember: MemberWrapper
        get() = members.firstOrNull { it.id == bot.id } ?: runBlocking {
            val data = botWrapper.impl.getGroupMemberInfo(id, bot.id, false).data
            MemberWrapper(botWrapper, this@GroupWrapper, data ?: GroupMemberInfoResp(
                id, bot.id, bot.nick, "", "", 0, "", 0, 0, 0, "member", false, "", 0, true, 0
            ))
        }
    override val owner: NormalMember
        get() = members.first { it.permission == MemberPermission.OWNER }
    override val roamingMessages: RoamingMessages
        get() = throw NotImplementedError("Onebot 未提供消息漫游接口")
    override val settings: GroupSettingsWrapper = GroupSettingsWrapper(this)

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
        val receipt = runCatching {
            val forward = messageChain.findForwardMessage()
            val messageIds = if (forward != null) {
                val nodes = OnebotMessages.serializeForwardNodes(forward.nodeList)
                val response = botWrapper.impl.sendGroupForwardMsg(id, nodes)
                response.data.safeMessageIds
            } else {
                val msg = OnebotMessages.serializeToOneBotJson(messageChain)
                val response = botWrapper.impl.sendGroupMsg(id, msg, false)
                response.data.safeMessageIds
            }

            OutgoingSource.group(
                bot = bot,
                ids = messageIds,
                internalIds = messageIds,
                isOriginalMessageInitialized = true,
                originalMessage = messageChain,
                sender = bot,
                target = this,
                time = currentTimeSeconds().toInt()
            ).receipt(this)
        }.onFailure { throwable = it }.getOrNull()
        GroupMessagePostSendEvent(this, messageChain, throwable, receipt).broadcast()

        bot.logger.verbose("Group($id) <- $messageChain")

        return receipt ?: throw throwable!!
    }

    override suspend fun setEssenceMessage(source: MessageSource): Boolean {
        checkBotPermission(MemberPermission.ADMINISTRATOR)
        botWrapper.impl.setEssenceMsg(source.ids[0])
        return true
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

    override fun toString(): String = "Group($id)"

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(
        "Please use files instead.",
        replaceWith = ReplaceWith("files.root"),
        level = DeprecationLevel.ERROR
    ) // deprecated since 2.8.0-RC
    @DeprecatedSinceMirai(warningSince = "2.8", errorSince = "2.14")
    override val filesRoot: net.mamoe.mirai.utils.RemoteFile
        get() = throw IllegalStateException("Deprecated")

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
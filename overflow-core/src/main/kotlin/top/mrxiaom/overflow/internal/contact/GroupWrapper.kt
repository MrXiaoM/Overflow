@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.contact

import cn.evolvefield.onebot.sdk.entity.Anonymous
import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.response.group.GroupInfoResp
import cn.evolvefield.onebot.sdk.response.group.GroupMemberInfoResp
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import cn.evolvefield.onebot.sdk.util.JsonHelper.nullable
import cn.evolvefield.onebot.sdk.util.data
import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.sdk.util.jsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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
import net.mamoe.mirai.spi.AudioToSilkService
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.contact.RemoteGroup
import top.mrxiaom.overflow.contact.RemoteUser
import top.mrxiaom.overflow.contact.Updatable
import top.mrxiaom.overflow.internal.contact.data.*
import top.mrxiaom.overflow.internal.contact.data.AnnouncementsWrapper.Companion.fetchAnnouncements
import top.mrxiaom.overflow.internal.contact.data.EssencesWrapper.Companion.fetchEssences
import top.mrxiaom.overflow.internal.contact.data.RemoteFilesWrapper.Companion.fetchFiles
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.groupMsg
import top.mrxiaom.overflow.internal.message.data.OutgoingSource.receipt
import top.mrxiaom.overflow.internal.utils.update
import top.mrxiaom.overflow.internal.utils.wrapAsMember
import top.mrxiaom.overflow.spi.FileService
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
internal class GroupWrapper(
    override val bot: BotWrapper,
    internal var impl: GroupInfoResp,
    internal var implJson: JsonElement,
) : Group, RemoteGroup, RemoteUser, Updatable, CanSendMessage {
    override val onebotData: String
        get() = gson.toJson(implJson)
    private var membersInternal: ContactList<MemberWrapper>? = null
    private var anonymousInternal: HashMap<String, AnonymousMemberWrapper> = hashMapOf()
    internal val emptyMessagesIdMap: HashMap<Long, Int> = hashMapOf()

    val data: GroupInfoResp
        get() = impl
    override suspend fun queryUpdate() {
        impl = bot.impl.getGroupInfo(impl.groupId, false).data ?: throw IllegalStateException("刷新群 ${impl.groupId} 的群信息失败")
    }

    override suspend fun updateAnnouncements(): Announcements {
        return announcements.also { it.update() }
    }

    /**
     * 请求刷新并获取群员信息。
     */
    internal suspend fun updateMember(userId: Long): MemberWrapper? {
        val result = bot.impl.getGroupMemberInfo(id, userId, false)
        val data = result.data ?: return null
        return updateMember(data, result.json.data ?: JsonObject())
    }

    /**
     * 通过指定的 GroupMemberInfoResp 刷新并获取群员信息。
     */
    internal fun updateMember(member: GroupMemberInfoResp, json: JsonElement): MemberWrapper {
        return (members[member.userId] ?: MemberWrapper(this, member, json).also { members.delegate.add(it) }).apply {
            impl = member
        }
    }

    /**
     * 刷新并获取匿名成员信息
     */
    internal fun updateAnonymous(member: Anonymous): AnonymousMemberWrapper {
        return (anonymousInternal[member.flag] ?: AnonymousMemberWrapper(this, member).also { anonymousInternal[member.flag] = it }).apply {
            impl = member
        }
    }

    /**
     * 获取群员信息，或者刷新群员信息。
     * 仅在找不到群员信息时请求刷新。
     */
    internal suspend fun queryMember(userId: Long): MemberWrapper? {
        if (userId == bot.id) return botAsMember
        return members[userId] ?: run {
            val result = bot.impl.getGroupMemberInfo(id, userId, false)
            val data = result.data ?: return null
            data.wrapAsMember(this@GroupWrapper, result.json.data ?: JsonObject())
        }
    }

    @JvmBlockingBridge
    override suspend fun updateGroupMemberList(): ContactList<MemberWrapper> {
        return (membersInternal ?: ContactList()).apply {
            val result = bot.impl.getGroupMemberList(id)
            val data = result.data.takeIf { !it.nullable.isNullOrEmpty() }
            val json = result.json.data as? JsonArray ?: JsonArray()
            val membersList = data?.map { member ->
                val memberJson = json.firstOrNull { memberJson ->
                    memberJson.jsonObject?.ignorable("user_id", 0L) == member.userId
                } ?: JsonObject()
                MemberWrapper(this@GroupWrapper, member, memberJson)
            }
            if (membersList != null) update(membersList) { setImpl(it.impl) }
            membersInternal = this
        }
    }

    override val coroutineContext: CoroutineContext = CoroutineName("(Bot/${bot.id})Group/$id")

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
                bot.impl.setGroupName(id, value)
                impl.groupName = value
            }
        }
    override val members: ContactList<MemberWrapper>
        get() = membersInternal ?: runBlocking {
            updateGroupMemberList()
        }
    override val botAsMember: MemberWrapper
        get() = members.firstOrNull { it.id == bot.id } ?: runBlocking {
            val result = bot.impl.getGroupMemberInfo(id, bot.id, false)
            val data = result.data
            if (data != null) {
                val json = result.json.data ?: JsonObject()
                MemberWrapper(this@GroupWrapper, data, json)
            } else {
                val impl = GroupMemberInfoResp().apply {
                    groupId = id
                    userId = bot.id
                    nickname = bot.nick
                }
                MemberWrapper(this@GroupWrapper, impl, gson.toJsonTree(impl))
            }
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
        bot.impl.setGroupLeave(id, false)
        return true
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        if (GroupMessagePreSendEvent(this, message).broadcast().isCancelled)
            throw EventCancelledException("消息发送已被取消")
        if (isBotMuted)
            throw BotIsBeingMutedException(this, message)

        val messageChain = message.toMessageChain()
        val (messageIds, throwable) = bot.sendMessageCommon(this, messageChain)
        val receipt = groupMsg(messageIds, messageChain).receipt(this)
        GroupMessagePostSendEvent(
            target = this,
            message = messageChain,
            exception = throwable,
            receipt = receipt.takeIf { throwable == null }
        ).broadcast()

        bot.logger.verbose("Group($id) <- $messageChain")

        return receipt
    }

    override suspend fun sendToOnebot(message: String): MsgId? {
        val resp = bot.impl.sendGroupMsg(id, message, false) {
            throwExceptions(true)
        }
        return resp.data
    }

    @JvmBlockingBridge
    override suspend fun setMsgReaction(messageId: Int, icon: String, enable: Boolean) {
        // Lagrange 在这块什么也不返回，所以忽略返回结果
        bot.impl.extGroupReaction(id, messageId, icon, enable)
    }

    override suspend fun setEssenceMessage(source: MessageSource): Boolean {
        checkBotPermission(MemberPermission.ADMINISTRATOR)
        bot.impl.setEssenceMsg(source.ids[0])
        return true
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

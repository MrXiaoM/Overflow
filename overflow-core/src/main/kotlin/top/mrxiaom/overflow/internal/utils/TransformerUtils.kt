@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.utils

import cn.evole.onebot.sdk.entity.Anonymous
import cn.evole.onebot.sdk.entity.MsgId
import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evole.onebot.sdk.response.contact.StrangerInfoResp
import cn.evole.onebot.sdk.response.group.GroupFilesResp
import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import cn.evole.onebot.sdk.response.misc.ClientsResp
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.hexToBytes
import top.mrxiaom.overflow.internal.contact.*
import top.mrxiaom.overflow.internal.contact.data.FileWrapper
import top.mrxiaom.overflow.internal.contact.data.FolderWrapper


/**
 * @param list 新的列表
 * @param updater this 是旧的，it 是新的，应当把新的内容放进旧的
 */
internal inline fun <reified T : Contact> ContactList<T>.update(
    list: List<T>?,
    updater: T.(T) -> Unit
) {
    if (list == null) return
    // 删除旧的
    delegate.removeIf { old -> list.none { old.id == it.id } }
    // 更新旧的
    delegate.mapNotNull { old -> list.firstOrNull { old.id == it.id }?.to(old) }.forEach { it.second.updater(it.first) }
    // 添加新的
    delegate.addAll(list.filterNot { delegate.any { old -> old.id == it.id } })
}

internal fun GroupMemberInfoResp.wrapAsMember(group: Group): MemberWrapper {
    return (group as GroupWrapper).updateMember(this)
}

internal fun GroupMessageEvent.GroupSender.wrapAsMember(group: Group): MemberWrapper {
    return GroupMemberInfoResp().also {
        it.groupId = group.id
        it.userId = userId.toLong()
        it.nickname = nickname
        it.card = card ?: ""
        it.sex = sex ?: ""
        it.age = age
        it.area = area ?: ""
        it.level = level?.toIntOrNull() ?: 0
        it.role = role ?: "member"
        it.title = title ?: ""
    }.wrapAsMember(group)
}

internal fun Anonymous.wrapAsMember(group: Group): AnonymousMemberWrapper {
    return (group as GroupWrapper).updateAnonymous(this)
}

internal suspend fun BotWrapper.group(groupId: Long): GroupWrapper {
    return getGroup(groupId) as? GroupWrapper ?: kotlin.run {
        val data = impl.getGroupInfo(groupId, false).data ?: throw IllegalStateException("无法取得群 $groupId 的信息")
        updateGroup(GroupWrapper(this, data))
    }
}


internal fun PrivateMessageEvent.PrivateSender.wrapAsFriend(bot: BotWrapper): FriendWrapper {
    return bot.updateFriend(FriendWrapper(bot, FriendInfoResp().also {
        it.userId = userId
        it.nickname = nickname
        it.remark = ""
    }))
}

internal fun StrangerInfoResp.wrapAsStranger(bot: BotWrapper): StrangerWrapper {
    return bot.updateStranger(StrangerWrapper(bot, this))
}

internal fun PrivateMessageEvent.PrivateSender.wrapAsStranger(bot: BotWrapper): StrangerWrapper {
    return StrangerInfoResp(
        userId,
        nickname,
        "",
        0,
        "",
        0,
        0,
        null
    ).wrapAsStranger(bot)
}

internal fun ClientsResp.Clients.wrapAsOtherClientInfo(): OtherClientInfo {
    val platform = Platform.getByTerminalId(loginPlatform.toInt())
    return OtherClientInfo(appId.toInt(), platform, deviceName, deviceKind)
}

internal val MsgId?.safeMessageIds: IntArray
    get() = this?.messageId?.run { intArrayOf(this) } ?: intArrayOf()

internal fun List<GroupFilesResp.Files>.toMiraiFiles(group: GroupWrapper, parent: FolderWrapper? = null): List<FileWrapper> {
    return map {
        val md5 = it.md5?.hexToBytes() ?: ByteArray(16)
        val sha1 = it.sha1?.hexToBytes() ?: ByteArray(16)
        FileWrapper(group, parent,
            it.fileId, it.fileName, md5, sha1, it.fileSize, it.deadTime, it.modifyTime, it.uploadTime, it.uploader, it.busid
        )
    }
}
internal fun List<GroupFilesResp.Folders>.toMiraiFolders(group: GroupWrapper, parent: FolderWrapper? = null): List<FolderWrapper> {
    return map {
        FolderWrapper(group, parent,
            it.folderId, it.folderName, it.createTime, it.createTime, it.creator, it.totalFileCount
        )
    }
}

package top.mrxiaom.overflow.internal.utils

import cn.evolvefield.onebot.sdk.entity.Anonymous
import cn.evolvefield.onebot.sdk.entity.GroupSender
import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.entity.PrivateSender
import cn.evolvefield.onebot.sdk.response.contact.FriendInfoResp
import cn.evolvefield.onebot.sdk.response.contact.StrangerInfoResp
import cn.evolvefield.onebot.sdk.response.group.GroupFilesResp
import cn.evolvefield.onebot.sdk.response.group.GroupMemberInfoResp
import cn.evolvefield.onebot.sdk.response.misc.ClientsResp
import cn.evolvefield.onebot.sdk.util.data
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.hexToBytes
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.internal.contact.*
import top.mrxiaom.overflow.internal.contact.data.FileWrapper
import top.mrxiaom.overflow.internal.contact.data.FolderWrapper


/**
 * @param list 新的列表
 * @param updater this 是旧的，it 是新的，应当把新的内容放进旧的
 */
internal inline fun <reified T : Contact> ContactList<T>.update(
    list: List<T>,
    updater: T.(T) -> Unit
) {
    // 删除旧的、在新的列表中不存在的
    delegate.removeIf { old -> list.none { old.id == it.id } }
    // 更新旧的
    delegate.mapNotNull { old -> list.firstOrNull { old.id == it.id }?.to(old) }
        .forEach { it.second.updater(it.first) }
    // 添加新的
    delegate.addAll(list.filterNot { delegate.any { old -> old.id == it.id } })
}

internal fun GroupMemberInfoResp.wrapAsMember(group: Group, json: JsonElement): MemberWrapper {
    return (group as GroupWrapper).updateMember(this, json)
}

internal suspend fun GroupSender.wrapAsMember(group: GroupWrapper): MemberWrapper? {
    val member = group.queryMember(userId.toLong())
    member?.impl?.also {
        it.nickname = nickname
        it.card = card
        if (it.role.isEmpty()) {
            it.role = role // sender.role 可能不可信 #139
        }
    }
    return member
}

internal fun Anonymous.wrapAsMember(group: Group): AnonymousMemberWrapper {
    return (group as GroupWrapper).updateAnonymous(this)
}

internal suspend fun BotWrapper.group(groupId: Long): GroupWrapper {
    return getGroup(groupId) as? GroupWrapper ?: kotlin.run {
        val result = impl.getGroupInfo(groupId, false)
        val data = result.data ?: throw IllegalStateException("无法取得群 $groupId 的信息")
        updateGroup(GroupWrapper(this, data, result.json.data ?: JsonObject()))
    }
}


internal fun PrivateSender.wrapAsFriend(bot: BotWrapper, json: JsonElement): FriendWrapper {
    return bot.updateFriend(FriendWrapper(bot, FriendInfoResp().also {
        it.userId = userId
        it.nickname = nickname
        it.sex = sex
        it.age = age
    }, json))
}

internal fun StrangerInfoResp.wrapAsStranger(bot: BotWrapper, json: JsonElement): StrangerWrapper {
    return bot.updateStranger(StrangerWrapper(bot, this, json))
}

internal fun PrivateSender.wrapAsStranger(bot: BotWrapper, json: JsonElement): StrangerWrapper {
    val id = userId
    val nick = nickname
    val priSex = sex
    val priAge = age
    return StrangerInfoResp().apply {
        userId = id
        nickname = nick
        sex = priSex
        age = priAge
    }.wrapAsStranger(bot, json)
}

internal fun ClientsResp.Clients.wrapAsOtherClientInfo(): OtherClientInfo {
    val platform = Platform.getByTerminalId(loginPlatform.toInt())
    return OtherClientInfo(appId.toInt(), platform, deviceName, deviceKind)
}

internal fun MsgId?.safeMessageIds(bot: RemoteBot): IntArray {
    return this?.messageId?.run { intArrayOf(this) } ?: throw IllegalStateException("消息发送失败，详见网络日志 (logs/onebot/*.log) 和 Onebot 实现 (${bot.appName} v${bot.appVersion}) 的日志")
}

internal fun List<GroupFilesResp.Files>.toMiraiFiles(group: GroupWrapper, parent: FolderWrapper? = null): List<FileWrapper> {
    return map { it.toMiraiFile(group, parent) }
}
internal fun GroupFilesResp.Files.toMiraiFile(group: GroupWrapper, parent: FolderWrapper? = null): FileWrapper {
    val md5 = md5?.hexToBytes() ?: ByteArray(16)
    val sha1 = sha1?.hexToBytes() ?: ByteArray(16)
    return FileWrapper(group, parent,
        fileId, fileName, md5, sha1, fileSize, deadTime, modifyTime, uploadTime, uploader, busid
    )
}
internal fun List<GroupFilesResp.Folders>.toMiraiFolders(group: GroupWrapper, parent: FolderWrapper? = null): List<FolderWrapper> {
    return map { it.toMiraiFolder(group, parent) }
}
internal fun GroupFilesResp.Folders.toMiraiFolder(group: GroupWrapper, parent: FolderWrapper? = null): FolderWrapper {
    return FolderWrapper(group, parent,
        folderId, folderName, createTime, createTime, creator, totalFileCount
    )
}

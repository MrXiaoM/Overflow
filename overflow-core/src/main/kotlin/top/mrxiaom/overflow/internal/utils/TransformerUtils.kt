package top.mrxiaom.overflow.internal.utils

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evole.onebot.sdk.response.contact.StrangerInfoResp
import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.contact.*


/**
 * @param list 新的列表
 * @param updater this 是旧的，it 是新的，应当把新的内容放进旧的
 */
@OptIn(MiraiInternalApi::class)
internal inline fun <reified T : Contact> ContactList<T>.update(
    list: List<T>,
    updater: T.(T) -> Unit
) {
    // 删除旧的
    delegate.removeIf { old -> list.none { old.id == it.id } }
    // 更新旧的
    delegate.mapNotNull { old -> list.firstOrNull { old.id == it.id }?.to(old) }.forEach { it.second.updater(it.first) }
    // 添加新的
    delegate.addAll(list.filterNot { delegate.any { old -> old.id == it.id } })
}

fun GroupMemberInfoResp.wrapAsMember(group: Group): MemberWrapper {
    return (group as GroupWrapper).updateMember(this)
}

fun GroupMessageEvent.GroupSender.wrapAsMember(group: Group): MemberWrapper {
    return GroupMemberInfoResp().also {
        it.groupId = group.id
        it.userId = userId.toLong()
        it.nickname = nickname
        it.card = card ?: ""
        it.sex = sex ?: ""
        it.age = age ?: 0
        it.area = area ?: ""
        it.level = level?.toIntOrNull() ?: 0
        it.role = role ?: "member"
        it.title = title ?: ""
    }.wrapAsMember(group)
}

suspend fun BotWrapper.group(groupId: Long): Group {
    return getGroup(groupId) ?: kotlin.run {
        val data = impl.getGroupInfo(groupId, false).data ?: throw IllegalStateException("无法取得群信息")
        updateGroup(GroupWrapper(this, data))
    }
}


fun PrivateMessageEvent.PrivateSender.wrapAsFriend(bot: BotWrapper): FriendWrapper {
    return bot.updateFriend(FriendWrapper(bot, FriendInfoResp().also {
        it.userId = userId
        it.nickname = nickname
        it.remark = ""
    }))
}

fun StrangerInfoResp.wrapAsStranger(bot: BotWrapper): StrangerWrapper {
    return bot.updateStranger(StrangerWrapper(bot, this))
}

fun PrivateMessageEvent.PrivateSender.wrapAsStranger(bot: BotWrapper): StrangerWrapper {
    return StrangerInfoResp(
        userId,
        nickname,
        "",
        0,
        "",
        0,
        0
    ).wrapAsStranger(bot)
}


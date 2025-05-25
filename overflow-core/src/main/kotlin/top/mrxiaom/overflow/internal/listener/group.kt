package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.client.handler.EventBus.listen
import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.sdk.event.notice.NotifyNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.group.*
import cn.evolvefield.onebot.sdk.event.notice.misc.GroupMsgEmojiLikeNotice
import cn.evolvefield.onebot.sdk.event.notice.misc.GroupReactionNotice
import cn.evolvefield.onebot.sdk.event.request.GroupAddRequestEvent
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import top.mrxiaom.overflow.event.MemberEssenceNoticeEvent
import top.mrxiaom.overflow.event.MessageReactionEvent
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.contact.GroupWrapper
import top.mrxiaom.overflow.internal.contact.MemberWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages.toMiraiMessage
import top.mrxiaom.overflow.internal.message.data.IncomingSource.groupMsg
import top.mrxiaom.overflow.internal.message.data.WrappedFileMessage
import top.mrxiaom.overflow.internal.utils.group
import top.mrxiaom.overflow.internal.utils.queryProfile
import top.mrxiaom.overflow.internal.utils.wrapAsMember

private fun BotWrapper.receiveGroupMessage(member: NormalMember, message: MessageChain, messageId: Int, time: Int) {
    Overflow.instance.resolveResourceDownload(message)
    val group = member.group
    val messageString = message.toString()
    val messageSource = bot.groupMsg(intArrayOf(messageId), message, member, time)
    val miraiMessage = messageSource.plus(message)
    if (member.id == bot.id) {
        bot.logger.verbose("[SYNC] [${group.name}(${group.id})] <- $messageString")
        @Suppress("DEPRECATION") // TODO: 无法获取到哪个客户端发送的消息
        bot.eventDispatcher.broadcastAsync(GroupMessageSyncEvent(
            group, miraiMessage, member, member.nameCardOrNick, messageSource.time
        ))
    } else {
        bot.logger.verbose("[${group.name}(${group.id})] ${member.nameCardOrNick}(${member.id}) -> $messageString")
        bot.eventDispatcher.broadcastAsync(GroupMessageEvent(
            member.nameCardOrNick, member.permission, member, miraiMessage, messageSource.time
        ))
    }
}

internal fun addGroupListeners() {
    listen<GroupMessageEvent>("normal") { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return@listen
        // 普通群消息
        val group = bot.group(e.groupId)
        val member = e.sender?.wrapAsMember(group) ?: return@listen

        val message = e.toMiraiMessage(bot)
        // 使用 group_upload 事件接收群文件时，忽略接收群文件类型消息
        if (bot.impl.config.useGroupUploadEventForFileMessage && (message.isEmpty() || message.any { it is FileMessage })) {
            group.emptyMessagesIdMap[member.id] = e.messageId
            return@listen
        }
        receiveGroupMessage(member, message, e.messageId, e.time())
    }
    listen<GroupMessageEvent>("anonymous") { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return@listen
        // 匿名群消息
        val group = bot.group(e.groupId)
        val member = e.anonymous?.wrapAsMember(group) ?: return@listen

        val message = e.toMiraiMessage(bot)
        // 匿名成员不会发文件
        if (bot.impl.config.useGroupUploadEventForFileMessage && (message.isEmpty() || message.any { it is FileMessage })) {
            return@listen
        }
        val messageString = message.toString()
        val messageSource = bot.groupMsg(intArrayOf(e.messageId), message, member, e.time())
        val miraiMessage = messageSource.plus(message)
        bot.logger.verbose("[${group.name}(${group.id})] ${member.nameCard}(${member.anonymousId}) -> $messageString")
        bot.eventDispatcher.broadcastAsync(
            GroupMessageEvent(
                member.nameCardOrNick, member.permission, member, miraiMessage, messageSource.time
            )
        )
    }
    listen<GroupMessageEvent>("notice") { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return@listen
        // 系统提示

        TODO("系统提示，如 管理员已禁止群内匿名聊天")
    }
    listen<GroupUploadNoticeEvent> { e ->
        // 群文件上传消息
        val file = e.file ?: return@listen
        if (bot.checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return@listen
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId)
        if (member == null) {
            bot.logger.warning("群文件上传事件找不到群员 ${e.groupId}:${e.userId}")
            return@listen
        }
        val messageId = group.emptyMessagesIdMap.remove(member.id)
        if (messageId == null) {
            bot.logger.warning("群文件上传事件找不到前置消息ID (来自群员 ${e.userId})")
            return@listen
        }
        val message = buildMessageChain {
            add(WrappedFileMessage(file.id, file.busId.toInt(), file.name, file.size, file.url))
        }
        receiveGroupMessage(member, message, messageId, e.time())
    }
    listen<GroupMsgDeleteNoticeEvent> { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.operatorId, "%onebot 返回了异常的数值 operator_id=%value")
            || checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")) return@listen
        // 消息撤回通知
        val group = bot.group(e.groupId)
        val operator = group.queryMember(e.operatorId)
        val target = group.queryMember(e.userId)
            ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        bot.eventDispatcher.broadcastAsync(
            MessageRecallEvent.GroupRecall(
                bot,
                target.id,
                intArrayOf(e.msgId.toInt()),
                intArrayOf(e.msgId.toInt()),
                e.timeInSecond().toInt(),
                operator, group,
                target
            )
        )
    }
    listen<GroupAddRequestEvent>("add") { e ->
        if (bot.checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return@listen
        // 某人申请入群
        bot.eventDispatcher.broadcastAsync(MemberJoinRequestEvent(
            bot = bot,
            eventId = Overflow.instance.putMemberJoinRequestFlag(e.flag),
            message = e.comment,
            fromId = e.userId,
            groupId = e.groupId,
            groupName = bot.getGroup(e.groupId)?.name ?: e.groupId.toString(),
            fromNick = e.userId.takeIf { it > 0 }?.run { bot.queryProfile(this) { nickname } } ?: "",
            invitorId = e.invitorId.takeIf { it > 0 } // LLOnebot: 获取邀请者
        ))
    }
    listen<GroupAddRequestEvent>("invite") { e ->
        if (bot.checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return@listen
        // 某人邀请机器人入群
        bot.eventDispatcher.broadcastAsync(BotInvitedJoinGroupRequestEvent(
            bot = bot,
            eventId = Overflow.instance.putInventedJoinGroupRequestFlag(e.flag),
            invitorId = e.userId,
            groupId = e.groupId,
            groupName = bot.getGroup(e.groupId)?.name ?: e.groupId.toString(),
            invitorNick = e.userId.takeIf { it > 0 }?.run { bot.queryProfile(this) { nickname } } ?: ""
        ))
    }
    suspend fun BotWrapper.checkDecreaseNotice(e: GroupDecreaseNoticeEvent): Pair<GroupWrapper, MemberWrapper>? {
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")
        ) return null
        val group = bot.group(e.groupId)
        val member = if (e.userId == bot.id) {
            group.botAsMember
        } else {
            group.queryMember(e.userId)
                ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        }
        return group to member
    }
    listen<GroupDecreaseNoticeEvent>("leave") { e ->
        val (group, member) = checkDecreaseNotice(e) ?: return@listen
        // 主动退群
        bot.eventDispatcher.broadcastAsync(MemberLeaveEvent.Quit(member))
        group.members.remove(member.id)
    }
    listen<GroupDecreaseNoticeEvent>("kick") { e ->
        val (group, member) = checkDecreaseNotice(e) ?: return@listen
        // 成员被踢
        if (bot.checkId(e.operatorId, "%onebot 返回了异常的数值 operator_id=%value")) return@listen
        bot.eventDispatcher.broadcastAsync(
            MemberLeaveEvent.Kick(
                member = member,
                operator = group.queryMember(e.operatorId)
            )
        )
        group.members.remove(member.id)
    }
    listen<GroupDecreaseNoticeEvent>("kick_me") { e ->
        val (group, _) = checkDecreaseNotice(e) ?: return@listen
        if (bot.checkId(e.operatorId, "%onebot 返回了异常的数值 operator_id=%value")) return@listen
        // 登录号被踢
        val operator = group.queryMember(e.operatorId)
        if (operator != null) {
            bot.eventDispatcher.broadcastAsync(BotLeaveEvent.Kick(operator))
            bot.groups.remove(e.groupId)
        }
    }

    suspend fun BotWrapper.checkGroupIncrease(e: GroupIncreaseNoticeEvent): Pair<GroupWrapper, MemberWrapper>? {
        if (bot.checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || bot.checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")) return null
        val group = bot.group(e.groupId)
        val member = if (e.userId == bot.id) {
            group.botAsMember
        } else {
            group.queryMember(e.userId)
                ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        }
        return group to member
    }
    listen<GroupIncreaseNoticeEvent>("approve") { e ->
        val (group, member) = checkGroupIncrease(e) ?: return@listen
        // 管理员已同意入群
        if (member.id == bot.id) {
            bot.eventDispatcher.broadcastAsync(BotJoinGroupEvent.Active(group))
        } else {
            bot.eventDispatcher.broadcastAsync(MemberJoinEvent.Active(member))
        }
    }
    listen<GroupIncreaseNoticeEvent>("invite") { e ->
        val (group, member) = checkGroupIncrease(e) ?: return@listen
        val invitor = group.queryMember(e.operatorId)
            ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.operatorId}")
        // 管理员已同意邀请入群
        if (member.id == bot.id) {
            bot.eventDispatcher.broadcastAsync(BotJoinGroupEvent.Invite(invitor))
        } else {
            bot.eventDispatcher.broadcastAsync(MemberJoinEvent.Invite(member, invitor))
        }
    }
    listen<NotifyNoticeEvent>("title") { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")) return@listen
        // 群头衔变更通知
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId)
            ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        val titleOld = member.impl.title
        member.impl.title = e.title
        bot.eventDispatcher.broadcastAsync(MemberSpecialTitleChangeEvent(
            origin = titleOld,
            new = e.title,
            member = member,
            operator = group.owner // 目前只有群主可以更改群头衔
        ))
    }
    suspend fun BotWrapper.checkGroupBan(e: GroupBanNoticeEvent): Pair<GroupWrapper, MemberWrapper?>? {
        if (bot.checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return null
        val group = bot.group(e.groupId)
        val operator = e.operatorId.takeIf { it > 0 }?.run { group.queryMember(this) }
        return group to operator
    }
    listen<GroupBanNoticeEvent>("ban") { e ->
        val (group, operator) = checkGroupBan(e) ?: return@listen
        // 禁言通知
        if (e.userId == 0L) { // 全员禁言
            val origin = group.settings.isMuteAll
            group.settings.muteAll = true
            if (operator == null && e.operatorId != bot.id) {
                return@listen
            }
            bot.eventDispatcher.broadcastAsync(GroupMuteAllEvent(
                origin = origin,
                new = true,
                group = group,
                operator = operator?.takeIf { operator.id != bot.id }
            ))
            return@listen
        }
        if (e.userId == bot.id) { // 机器人禁言
            if (operator == null) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员 ${e.operatorId}")
                return@listen
            }
            group.botAsMember.impl.shutUpTimestamp = e.timeInSecond() + e.duration
            bot.eventDispatcher.broadcastAsync(BotMuteEvent(
                durationSeconds = e.duration.toInt(),
                operator = operator
            ))
        } else { // 其他人禁言
            if (operator == null && e.operatorId != bot.id) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员(操作者) ${e.operatorId}")
                return@listen
            }
            val member = group.updateMember(e.userId)
            if (member == null) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员 ${e.userId}")
                return@listen
            }
            bot.eventDispatcher.broadcastAsync(MemberMuteEvent(
                member = member,
                durationSeconds = e.duration.toInt(),
                operator = operator?.takeIf { operator.id != bot.id }
            ))
        }
    }
    listen<GroupBanNoticeEvent>("lift_ban") { e ->
        val (group, operator) = checkGroupBan(e) ?: return@listen
        // 解除禁言通知
        if (e.userId == 0L) { // 全员禁言
            val origin = group.settings.isMuteAll
            group.settings.muteAll = false
            if (operator == null && e.operatorId != bot.id) {
                return@listen
            }
            bot.eventDispatcher.broadcastAsync(GroupMuteAllEvent(
                origin = origin,
                new = false,
                group = group,
                operator = operator?.takeIf { operator.id != bot.id }
            ))
            return@listen
        }
        if (e.userId == bot.id) { // 机器人禁言
            if (operator == null) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员 ${e.operatorId}")
                return@listen
            }
            group.botAsMember.impl.shutUpTimestamp = 0L
            bot.eventDispatcher.broadcastAsync(BotUnmuteEvent(
                operator = operator
            ))
        } else { // 其他人禁言
            if (operator == null && e.operatorId != bot.id) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员(操作者) ${e.operatorId}")
                return@listen
            }
            val member = group.updateMember(e.userId)
            if (member == null) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员 ${e.userId}")
                return@listen
            }
            bot.eventDispatcher.broadcastAsync(MemberUnmuteEvent(
                member = member,
                operator = operator?.takeIf { operator.id != bot.id }
            ))
        }
    }
    listen<GroupAdminNoticeEvent> { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")) return@listen
        // 设置/取消管理员通知
        val group = bot.group(e.groupId)
        val isBot = e.userId == bot.id
        val member = if (isBot) {
            group.botAsMember
        } else {
            group.queryMember(e.userId) ?: return@listen
        }
        val (origin, new) = when (e.subType) {
            "set" -> {
                member.impl.role = "admin"
                MemberPermission.MEMBER to MemberPermission.ADMINISTRATOR
            }
            "unset" -> {
                member.impl.role = "member"
                MemberPermission.ADMINISTRATOR to MemberPermission.MEMBER
            }
            else -> return@listen
        }
        val event = if (isBot) BotGroupPermissionChangeEvent(
            group = group,
            origin = origin,
            new = new
        ) else MemberPermissionChangeEvent(
            member = member,
            origin = origin,
            new = new
        )
        bot.eventDispatcher.broadcastAsync(event)
    }
    listen<GroupEssenceNoticeEvent> { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.operatorId, "%onebot 返回了异常的数值 operator_id=%value")
            || checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")) return@listen
        // 群精华消息通知
        val group = bot.group(e.groupId)
        val sender = group.queryMember(e.senderId) ?: return@listen
        val operator = group.queryMember(e.operatorId) ?: return@listen
        if (e.messageId <= 0) return@listen
        bot.eventDispatcher.broadcastAsync(when (e.subType) {
            "add" -> MemberEssenceNoticeEvent.Add(
                bot, group, sender, operator, e.messageId)
            "delete" -> MemberEssenceNoticeEvent.Delete(
                bot, group, sender, operator, e.messageId)
            else -> return@listen
        })
    }
    listen<GroupCardChangeNoticeEvent> { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")) return@listen
        // 群名片变更通知
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId) ?: return@listen
        member.impl.card = e.cardNew
        bot.eventDispatcher.broadcastAsync(MemberCardChangeEvent(
            e.cardOld, e.cardNew, member
        ))
    }
    listen<GroupNameChangeNoticeEvent> { e ->
        if (bot.checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")) return@listen
        // 群名称变更通知
        val group = bot.group(e.groupId)
        val origin = group.name
        val new = e.name
        group.impl.groupName = new
        val operator = if (e.operatorId <= 0) null else group.queryMember(e.operatorId)
        bot.eventDispatcher.broadcastAsync(GroupNameChangeEvent(
                origin, new, group, operator
        ))
    }
    listen<GroupMsgEmojiLikeNotice> { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.userId, "%onebot 返回了异常的数值 user_id=%value")) return@listen
        // 消息表情回应通知（LLOnebot, NapCat）
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId) ?: return@listen
        val operator = if (member.id == bot.id) null else member
        // TODO: 接口中没有提及是添加还是删除，可能要开一张缓存表来记录
        for (like in e.likes) {
            bot.eventDispatcher.broadcastAsync(MessageReactionEvent(
                bot = bot,
                group = group,
                messageId = e.messageId,
                operator = operator,
                reaction = like.emojiId,
                count = like.count,
                operation = true, // TODO
            ))
        }
    }
    listen<GroupReactionNotice> { e ->
        if (checkId(e.groupId, "%onebot 返回了异常的数值 group_id=%value")
            || checkId(e.operatorId, "%onebot 返回了异常的数值 operator_id=%value")) return@listen
        // 消息表情回应通知（Lagrange）
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.operatorId) ?: return@listen
        val operation = when(e.subType) {
            "add" -> true
            "remove" -> false
            else -> return@listen
        }
        val operator = if (member.id == bot.id) null else member
        bot.eventDispatcher.broadcastAsync(MessageReactionEvent(
            bot = bot,
            group = group,
            messageId = e.messageId,
            operator = operator,
            reaction = e.code,
            count = e.count,
            operation = operation,
        ))
    }
}

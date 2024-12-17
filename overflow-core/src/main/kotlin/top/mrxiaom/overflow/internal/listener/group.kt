@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.sdk.event.notice.group.*
import cn.evolvefield.onebot.sdk.event.request.GroupAddRequestEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.sdk.util.jsonObject
import com.google.gson.JsonObject
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.event.MemberEssenceNoticeEvent
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages.toMiraiMessage
import top.mrxiaom.overflow.internal.message.data.IncomingSource
import top.mrxiaom.overflow.internal.message.data.WrappedFileMessage
import top.mrxiaom.overflow.internal.utils.bot
import top.mrxiaom.overflow.internal.utils.group
import top.mrxiaom.overflow.internal.utils.queryProfile
import top.mrxiaom.overflow.internal.utils.wrapAsMember

internal fun addGroupListeners() {
    listOf(
        GroupMessageListener(),
        GroupMessageRecallListener(),
        GroupAddRequestListener(),
        GroupDecreaseNoticeListener(),
        GroupIncreaseNoticeListener(),
        GroupTitleChangeNoticeListener(),
        GroupBanNoticeListener(),
        GroupAdminNoticeListener(),
        GroupEssenceNoticeListener(),
        GroupCardChangeNoticeListener(),
        GroupNameChangeListener(),
        GroupUploadNoticeListener(),

    ).forEach(EventBus::addListener)
}

internal class GroupMessageListener : EventListener<GroupMessageEvent> {
    override suspend fun onMessage(e: GroupMessageEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        }) return
        when(e.subType) {
            "normal" -> {
                val group = bot.group(e.groupId)
                val json = e.json.jsonObject?.get("sender") as? JsonObject
                val member = e.sender?.wrapAsMember(group, json ?: JsonObject()) ?: return

                val message = e.toMiraiMessage(bot)
                // 使用 group_upload 事件接收群文件时，忽略接收群文件类型消息
                if (bot.impl.config.useGroupUploadEventForFileMessage && (message.isEmpty() || message.any { it is FileMessage })) {
                    group.emptyMessagesIdMap.put(member.id, e.messageId)
                    return
                }
                receiveGroupMessage(bot, member, message, e.messageId, e.timeInSecond())
            }
            "anonymous" -> {
                val group = bot.group(e.groupId)
                val member = e.anonymous?.wrapAsMember(group) ?: return

                val message = e.toMiraiMessage(bot)
                // 匿名成员不会发文件
                if (bot.impl.config.useGroupUploadEventForFileMessage && (message.isEmpty() || message.any { it is FileMessage })) {
                    return
                }
                val messageString = message.toString()
                val messageSource = IncomingSource.group(
                    bot = bot,
                    ids = intArrayOf(e.messageId),
                    internalIds = intArrayOf(e.messageId),
                    isOriginalMessageInitialized = true,
                    originalMessage = message,
                    sender = member,
                    time = e.timeInSecond().toInt()
                )
                val miraiMessage = messageSource.plus(message)
                bot.logger.verbose("[${group.name}(${group.id})] ${member.nameCard}(${member.anonymousId}) -> $messageString")
                bot.eventDispatcher.broadcastAsync(
                    GroupMessageEvent(
                        member.nameCardOrNick, member.permission, member, miraiMessage, messageSource.time
                    )
                )
            }
            "notice" -> {
                TODO("系统提示，如 管理员已禁止群内匿名聊天")
            }
        }
    }

    companion object {
        internal fun receiveGroupMessage(bot: BotWrapper, member: NormalMember, message: MessageChain, messageId: Int, timeInSecond: Long) {
            val group = member.group
            val messageString = message.toString()
            val messageSource = IncomingSource.group(
                bot = bot,
                ids = intArrayOf(messageId),
                internalIds = intArrayOf(messageId),
                isOriginalMessageInitialized = true,
                originalMessage = message,
                sender = member,
                time = timeInSecond.toInt()
            )
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

    }
}

internal class GroupMessageRecallListener : EventListener<GroupMsgDeleteNoticeEvent> {
    override suspend fun onMessage(e: GroupMsgDeleteNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        } || bot.checkId(e.operatorId) {
            "%onebot 返回了异常的数值 operator_id=%value"
        } || bot.checkId(e.userId) {
            "%onebot 返回了异常的数值 user_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val operator = group.queryMember(e.operatorId)
        val target = group.queryMember(e.userId) ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        bot.eventDispatcher.broadcastAsync(MessageRecallEvent.GroupRecall(bot,
            target.id,
            intArrayOf(e.msgId.toInt()),
            intArrayOf(e.msgId.toInt()),
            e.timeInSecond().toInt(),
            operator, group,
            target
        ))
    }
}

internal class GroupAddRequestListener : EventListener<GroupAddRequestEvent> {
    override suspend fun onMessage(e: GroupAddRequestEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        }) return
        when (e.subType) {
            "add" -> { // 某人申请入群
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
            "invite" -> { // 某人邀请机器人入群
                bot.eventDispatcher.broadcastAsync(BotInvitedJoinGroupRequestEvent(
                    bot = bot,
                    eventId = Overflow.instance.putInventedJoinGroupRequestFlag(e.flag),
                    invitorId = e.userId,
                    groupId = e.groupId,
                    groupName = bot.getGroup(e.groupId)?.name ?: e.groupId.toString(),
                    invitorNick = e.userId.takeIf { it > 0 }?.run { bot.queryProfile(this) { nickname } } ?: ""
                ))
            }
        }
    }
}

internal class GroupDecreaseNoticeListener : EventListener<GroupDecreaseNoticeEvent> {
    override suspend fun onMessage(e: GroupDecreaseNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        } || bot.checkId(e.userId) {
            "%onebot 返回了异常的数值 user_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId) ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        when (e.subType) {
            "leave" -> { // 主动退群
                bot.eventDispatcher.broadcastAsync(MemberLeaveEvent.Quit(member))
            }
            "kick" -> { // 成员被踢
                if (bot.checkId(e.operatorId) {
                    "%onebot 返回了异常的数值 operator_id=%value"
                }) return
                bot.eventDispatcher.broadcastAsync(MemberLeaveEvent.Kick(
                    member = member,
                    operator = group.queryMember(e.operatorId)
                ))
            }
            "kick_me" -> { // 登录号被踢
                // Mirai没有这个Event
            }
        }
    }
}

internal class GroupIncreaseNoticeListener : EventListener<GroupIncreaseNoticeEvent> {
    override suspend fun onMessage(e: GroupIncreaseNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        } || bot.checkId(e.userId) {
            "%onebot 返回了异常的数值 user_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId) ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        when (e.subType) {
            "approve" -> { // 管理员已同意入群
                bot.eventDispatcher.broadcastAsync(MemberJoinEvent.Active(member))
            }
            "invite" -> { // 管理员邀请入群
                bot.eventDispatcher.broadcastAsync(MemberJoinEvent.Invite(
                    member = member,
                    invitor = group.queryMember(e.operatorId) ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.operatorId}")
                ))
            }
        }
    }
}

internal class GroupTitleChangeNoticeListener : EventListener<GroupTitleChangeNoticeEvent> {
    override suspend fun onMessage(e: GroupTitleChangeNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        } || bot.checkId(e.userId) {
            "%onebot 返回了异常的数值 user_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId) ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
        MemberSpecialTitleChangeEvent(
            origin = e.titleOld,
            new = e.titleNew,
            member = member,
            operator = group.owner // 目前只有群主可以更改群头衔
        )
    }
}

internal class GroupBanNoticeListener : EventListener<GroupBanNoticeEvent> {
    override suspend fun onMessage(e: GroupBanNoticeEvent) {
        val bot = e.bot ?: return
        val mute = when(e.subType) {
            "ban" -> true
            "lift_ban" -> false
            else -> return
        }
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val operator = e.operatorId.takeIf { it > 0 }?.run { group.queryMember(this) }
        if (e.userId == 0L && e.duration == -1L) {
            val origin = group.settings.isMuteAll
            group.settings.muteAll = mute
            if (operator == null && e.operatorId != bot.id) {
                return
            }
            bot.eventDispatcher.broadcastAsync(GroupMuteAllEvent(
                origin = origin,
                new = mute,
                group = group,
                operator = operator
            ))
            return
        }
        if (e.userId == bot.id) {
            if (operator == null) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员 ${e.operatorId}")
                return
            }
            if (mute) {
                bot.eventDispatcher.broadcastAsync(BotMuteEvent(
                    durationSeconds = e.duration.toInt(),
                    operator = operator
                ))
            } else {
                bot.eventDispatcher.broadcastAsync(BotUnmuteEvent(
                    operator = operator
                ))
            }
        } else {
            if (operator == null && e.operatorId != bot.id) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员(操作者) ${e.operatorId}")
                return
            }
            val member = group.queryMember(e.userId)
            if (member == null) {
                bot.logger.warning("无法找到群 ${e.groupId} 的成员 ${e.userId}")
                return
            }
            if (mute) {
                bot.eventDispatcher.broadcastAsync(MemberMuteEvent(
                    member = member,
                    durationSeconds = e.duration.toInt(),
                    operator = operator
                ))
            } else {
                bot.eventDispatcher.broadcastAsync(MemberUnmuteEvent(
                    member = member,
                    operator = operator
                ))
            }
        }
    }
}
internal class GroupAdminNoticeListener : EventListener<GroupAdminNoticeEvent> {
    override suspend fun onMessage(e: GroupAdminNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        } || bot.checkId(e.userId) {
            "%onebot 返回了异常的数值 user_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId) ?: return
        val origin = member.permission
        when (e.subType) {
            "set" -> {
                member.impl.role = "admin"
            }
            "unset" -> {
                member.impl.role = "member"
            }
        }
        if (origin != member.permission) {
            bot.eventDispatcher.broadcastAsync(MemberPermissionChangeEvent(
                member = member,
                origin = origin,
                new = member.permission
            ))
        }
    }
}
internal class GroupEssenceNoticeListener : EventListener<GroupEssenceNoticeEvent> {
    override suspend fun onMessage(e: GroupEssenceNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        } || bot.checkId(e.operatorId) {
            "%onebot 返回了异常的数值 operator_id=%value"
        } || bot.checkId(e.userId) {
            "%onebot 返回了异常的数值 user_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val sender = group.queryMember(e.senderId) ?: return
        val operator = group.queryMember(e.operatorId) ?: return
        if (e.messageId <= 0) return
        val event = when (e.subType) {
            "add" -> MemberEssenceNoticeEvent.Add(
                bot, group, sender, operator, e.messageId
            )
            "delete" -> MemberEssenceNoticeEvent.Delete(
                bot, group, sender, operator, e.messageId
            )
            else -> return
        }
        bot.eventDispatcher.broadcastAsync(event)
    }
}
internal class GroupCardChangeNoticeListener : EventListener<GroupCardChangeNoticeEvent> {
    override suspend fun onMessage(e: GroupCardChangeNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
            "%onebot 返回了异常的数值 group_id=%value"
        } || bot.checkId(e.userId) {
            "%onebot 返回了异常的数值 user_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId) ?: return
        bot.eventDispatcher.broadcastAsync(MemberCardChangeEvent(
            e.cardOld, e.cardNew, member
        ))
    }
}
internal class GroupNameChangeListener : EventListener<GroupNameChangeNoticeEvent> {
    override suspend fun onMessage(e: GroupNameChangeNoticeEvent) {
        val bot = e.bot ?: return
        if (bot.checkId(e.groupId) {
                "%onebot 返回了异常的数值 group_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val origin = group.name
        val new = e.name
        group.impl.groupName = new
        val operator = if (e.operatorId <= 0) null else group.queryMember(e.operatorId)
        bot.eventDispatcher.broadcastAsync(GroupNameChangeEvent(
                origin, new, group, operator
        ))
    }
}
internal class GroupUploadNoticeListener : EventListener<GroupUploadNoticeEvent> {
    override suspend fun onMessage(e: GroupUploadNoticeEvent) {
        val bot = e.bot ?: return
        val file = e.file ?: return
        if (bot.checkId(e.groupId) {
                "%onebot 返回了异常的数值 group_id=%value"
        }) return
        val group = bot.group(e.groupId)
        val member = group.queryMember(e.userId)
        if (member == null) {
            bot.logger.warning("群文件上传事件找不到群员 ${e.groupId}:${e.userId}")
            return
        }
        val messageId = group.emptyMessagesIdMap.remove(member.id)
        if (messageId == null) {
            bot.logger.warning("群文件上传事件找不到前置消息ID (来自群员 ${e.userId})")
            return
        }
        val message = buildMessageChain {
            add(WrappedFileMessage(file.id, file.busId.toInt(), file.name, file.size, file.url))
        }
        GroupMessageListener.receiveGroupMessage(bot, member, message, messageId, e.timeInSecond())
    }
}

@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evole.onebot.sdk.event.notice.group.GroupBanNoticeEvent
import cn.evole.onebot.sdk.event.notice.group.GroupMsgDeleteNoticeEvent
import cn.evole.onebot.sdk.event.notice.group.GroupNotifyNoticeEvent
import cn.evole.onebot.sdk.event.notice.group.GroupTitleChangeNoticeEvent
import cn.evole.onebot.sdk.event.request.GroupAddRequestEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.utils.*

fun EventBus.addGroupListeners(bot: BotWrapper) {
    listOf(
        GroupMessageListener(bot),
        GroupNotifyListener(bot),
        GroupMessageRecallListener(bot),
        GroupAddRequestListener(bot),
        GroupTitleChangeNoticeListener(bot),
        GroupBanNoticeListener(bot),

    ).forEach(::addListener)
}

internal class GroupMessageListener(
    val bot: BotWrapper
) : EventListener<GroupMessageEvent> {
    override suspend fun onMessage(e: GroupMessageEvent) {
        when(e.subType) {
            "normal" -> {
                val group = bot.group(e.groupId)
                val member = e.sender.wrapAsMember(group)

                var miraiMessage = OnebotMessages.deserializeFromOneBot(bot, e.message)
                val messageString = miraiMessage.toString()
                val messageSource = object : OnlineMessageSource.Incoming.FromGroup() {
                    override val bot: Bot = this@GroupMessageListener.bot
                    override val ids: IntArray = intArrayOf(e.messageId)
                    override val internalIds: IntArray = ids
                    override val isOriginalMessageInitialized: Boolean = true
                    override val originalMessage: MessageChain = miraiMessage
                    override val sender: Member = member
                    override val time: Int = (e.time / 1000).toInt()
                }
                miraiMessage = messageSource.plus(miraiMessage)
                if (member.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                } else {
                    bot.logger.verbose("[${group.name}(${group.id})] ${member.nameCardOrNick}(${member.id}) -> $messageString")
                    GroupMessageEvent(
                        member.nameCardOrNick, member.permission, member, miraiMessage, messageSource.time
                    ).broadcast()
                }
            }
            "anonymous" -> {
                TODO("匿名消息")
            }
            "notice" -> {
                TODO("系统提示，如 管理员已禁止群内匿名聊天")
            }
        }
    }
}

internal class GroupNotifyListener(
    val bot: BotWrapper
) : EventListener<GroupNotifyNoticeEvent> {
    override suspend fun onMessage(e: GroupNotifyNoticeEvent) {
        val group = bot.group(e.groupId)
        when (e.subType) {
            "poke" -> {
                val operator = group.members[e.operatorId] ?: throw IllegalStateException("群 ${group.id} 戳一戳 无法获取操作者")
                val target = group.members[e.targetId] ?: throw IllegalStateException("群 ${group.id} 戳一戳 无法获取目标")
                // TODO: 戳一戳无法获取被戳一方的动作、后缀信息
                NudgeEvent(operator, target, group, "拍了拍", "").broadcast()
            }
        }
    }
}

internal class GroupMessageRecallListener(
    val bot: BotWrapper
): EventListener<GroupMsgDeleteNoticeEvent> {
    override suspend fun onMessage(e: GroupMsgDeleteNoticeEvent) {
        val group = bot.group(e.groupId)
        val operator = group.members[e.operatorId]
        MessageRecallEvent.GroupRecall(bot,
            bot.id, // TODO: Onebot 无法获取被撤回消息的发送者
            intArrayOf(e.msgId.toInt()),
            intArrayOf(e.msgId.toInt()),
            (e.time / 1000).toInt(),
            operator, group,
            group.botAsMember // TODO: Onebot 无法获取被撤回消息的发送者
        ).broadcast()
    }
}

internal class GroupAddRequestListener(
    val bot: BotWrapper
): EventListener<GroupAddRequestEvent> {
    override suspend fun onMessage(e: GroupAddRequestEvent) {
        when (e.subType) {
            "add" -> {
                MemberJoinRequestEvent(
                    bot = bot,
                    eventId = Overflow.instance.putMemberJoinRequestFlag(e.flag),
                    message = e.comment,
                    fromId = e.userId,
                    groupId = e.groupId,
                    groupName = bot.getGroup(e.groupId)?.name ?: e.groupId.toString(),
                    fromNick = bot.queryProfile(e.userId) { nickname } ?: "",
                    invitorId = null // TODO: 获取邀请者
                ).broadcast()
            }
            "invite" -> {
                BotInvitedJoinGroupRequestEvent(
                    bot = bot,
                    eventId = Overflow.instance.putInventedJoinGroupRequestFlag(e.flag),
                    invitorId = e.userId,
                    groupId = e.groupId,
                    groupName = bot.getGroup(e.groupId)?.name ?: e.groupId.toString(),
                    invitorNick = bot.queryProfile(e.userId) { nickname } ?: ""
                ).broadcast()
            }
        }
    }
}

internal class GroupTitleChangeNoticeListener(
    val bot: BotWrapper
): EventListener<GroupTitleChangeNoticeEvent> {
    override suspend fun onMessage(e: GroupTitleChangeNoticeEvent) {
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

internal class GroupBanNoticeListener(
    val bot: BotWrapper
): EventListener<GroupBanNoticeEvent> {
    override suspend fun onMessage(e: GroupBanNoticeEvent) {
        val mute = when(e.subType) {
            "ban" -> true
            "lift_ban" -> false
            else -> return
        }
        val group = bot.group(e.groupId)
        if (e.userId == bot.id) {
            val operator = group.queryMember(e.operatorId) ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
            if (mute) {
                BotMuteEvent(
                    durationSeconds = e.duration.toInt(),
                    operator = operator
                ).broadcast()
            } else {
                BotUnmuteEvent(
                    operator = operator
                ).broadcast()
            }
        } else {
            val operator = group.queryMember(e.operatorId)
            val member = group.queryMember(e.userId) ?: throw IllegalStateException("无法找到群 ${e.groupId} 的成员 ${e.userId}")
            if (mute) {
                MemberMuteEvent(
                    member = member,
                    durationSeconds = e.duration.toInt(),
                    operator = operator
                ).broadcast()
            } else {
                MemberUnmuteEvent(
                    member = member,
                    operator = operator
                ).broadcast()
            }
        }
    }

}
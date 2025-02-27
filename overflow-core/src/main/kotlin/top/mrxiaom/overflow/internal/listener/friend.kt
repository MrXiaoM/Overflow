@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.client.handler.EventBus.listen
import cn.evolvefield.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.sdk.event.notice.friend.PrivateMsgDeleteNoticeEvent
import cn.evolvefield.onebot.sdk.event.request.FriendAddRequestEvent
import net.mamoe.mirai.contact.remarkOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.message.OnebotMessages.toMiraiMessage
import top.mrxiaom.overflow.internal.message.data.IncomingSource.friendMsg
import top.mrxiaom.overflow.internal.message.data.IncomingSource.strangerMsg
import top.mrxiaom.overflow.internal.message.data.IncomingSource.tempMsg
import top.mrxiaom.overflow.internal.utils.group
import top.mrxiaom.overflow.internal.utils.queryProfile
import top.mrxiaom.overflow.internal.utils.wrapAsFriend
import top.mrxiaom.overflow.internal.utils.wrapAsStranger

internal fun addFriendListeners() {
    listen<PrivateMessageEvent>("friend") { e ->
        // 好友消息
        val friend = e.sender.wrapAsFriend(bot, e.json)

        if (friend.id == bot.id) {
            // TODO: 过滤自己发送的消息
            return@listen
        }
        val message = e.toMiraiMessage(bot)
        Overflow.instance.resolveResourceDownload(message)
        val messageString = message.toString()
        val messageSource = bot.friendMsg(intArrayOf(e.messageId), message, friend, e.time())
        val miraiMessage = messageSource.plus(message)
        bot.logger.verbose("${friend.remarkOrNick}(${friend.id}) -> $messageString")
        bot.eventDispatcher.broadcastAsync(FriendMessageEvent(
            friend, miraiMessage, messageSource.time
        ))
    }
    listen<PrivateMessageEvent>("group") { e ->
        // 群临时消息
        if (listOf("shamrock", "go-cqhttp").contains(bot.appName.lowercase())
            && !listOf(/*Group:*/0, /*Discussion:*/7).contains(e.tempSource)
        ) return@listen
        if (e.groupId <= 0) return@listen
        val group = bot.group(e.groupId)
        if (checkId(e.userId, "%onebot 返回了异常的 user_id=%value")) return@listen
        val member = group.queryMember(e.userId)

        if (member == null || member.id == bot.id) {
            // TODO: 过滤自己发送的消息
            return@listen
        }
        val message = e.toMiraiMessage(bot)
        val messageString = message.toString()
        val messageSource = bot.tempMsg(intArrayOf(e.messageId), message, member, e.time())
        val miraiMessage = messageSource.plus(message)
        bot.logger.verbose("[群临时消息] ${member.remarkOrNick}(${member.id}) -> $messageString")
        bot.eventDispatcher.broadcastAsync(GroupTempMessageEvent(
            member, miraiMessage, messageSource.time
        ))
    }
    listen<PrivateMessageEvent>("other") { e ->
        // 陌生人消息
        val stranger = e.sender.wrapAsStranger(bot, e.json)

        if (stranger.id == bot.id) {
            // TODO: 过滤自己发送的消息
            return@listen
        }
        val message = e.toMiraiMessage(bot)
        val messageString = message.toString()
        val messageSource = bot.strangerMsg(intArrayOf(e.messageId), message, stranger, e.time())
        val miraiMessage = messageSource.plus(message)
        bot.logger.verbose("${stranger.remarkOrNick}(${stranger.id}) -> $messageString")
        bot.eventDispatcher.broadcastAsync(StrangerMessageEvent(
            stranger, miraiMessage, messageSource.time
        ))
    }
    listen<FriendAddRequestEvent> { e ->
        // 好友添加申请
        eventDispatcher.broadcastAsync(NewFriendRequestEvent(
            bot = bot,
            eventId = Overflow.instance.putNewFriendRequestFlag(e.flag),
            message = e.comment,
            fromId = e.userId,
            fromGroupId = 0, // TODO: 获取来自哪个群
            fromNick = e.userId.takeIf { it > 0 }?.run { queryProfile(this) { nickname } } ?: ""
        ))
    }
    listen<PrivateMsgDeleteNoticeEvent> { e ->
        // 好友撤回消息通知
        val operatorId = e.operatorId.takeIf { it > 0 } ?: e.userId
        val friend = e.userId.takeIf { it > 0 }?.run { bot.getFriend(this) }
        if (friend == null) {
            logger.warning("无法找到好友 ${e.userId}")
            return@listen
        }
        eventDispatcher.broadcastAsync(MessageRecallEvent.FriendRecall(
            bot,
            intArrayOf(e.msgId.toInt()),
            intArrayOf(e.msgId.toInt()),
            e.timeInSecond().toInt(),
            operatorId,
            friend
        ))
    }
}

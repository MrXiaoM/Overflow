@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.sdk.event.notice.friend.PrivateMsgDeleteNoticeEvent
import cn.evolvefield.onebot.sdk.event.request.FriendAddRequestEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.contact.remarkOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.message.OnebotMessages.toMiraiMessage
import top.mrxiaom.overflow.internal.message.data.IncomingSource
import top.mrxiaom.overflow.internal.utils.*

internal fun addFriendListeners() {
    listOf(
        FriendMessageListener(),
        FriendAddRequestListener(),
        FriendMessageRecallListener(),

    ).forEach(EventBus::addListener)
}

internal class FriendMessageListener : EventListener<PrivateMessageEvent> {
    override suspend fun onMessage(e: PrivateMessageEvent) {
        val bot = e.bot ?: return
        when (e.subType) {
            "friend" -> {
                val friend = e.sender.wrapAsFriend(bot)

                if (friend.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                    return
                }
                val message = e.toMiraiMessage(bot)
                val messageString = message.toString()
                val messageSource = IncomingSource.friend(
                    bot = bot,
                    ids = intArrayOf(e.messageId),
                    internalIds = intArrayOf(e.messageId),
                    isOriginalMessageInitialized = true,
                    originalMessage = message,
                    sender = friend,
                    subject = friend,
                    target = bot,
                    time = (e.time / 1000).toInt()
                )
                val miraiMessage = messageSource.plus(message)
                bot.logger.verbose("${friend.remarkOrNick}(${friend.id}) -> $messageString")
                bot.eventDispatcher.broadcastAsync(FriendMessageEvent(
                    friend, miraiMessage, messageSource.time
                ))
            }
            "group" -> {
                if (listOf("shamrock", "go-cqhttp").contains(bot.appName.lowercase())
                    && !listOf(/*Group:*/0, /*Discussion:*/7).contains(e.tempSource)) return
                if (e.groupId <= 0) return
                val group = bot.group(e.groupId)
                val member = group.queryMember(e.userId)

                if (member == null || member.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                    return
                }
                val message = e.toMiraiMessage(bot)
                val messageString = message.toString()
                val messageSource = IncomingSource.temp(
                    bot = bot,
                    ids = intArrayOf(e.messageId),
                    internalIds = intArrayOf(e.messageId),
                    originalMessage = message,
                    sender = member,
                    subject = member,
                    isOriginalMessageInitialized = true,
                    target = bot,
                    time = (e.time / 1000).toInt()
                )
                val miraiMessage = messageSource.plus(message)
                bot.logger.verbose("[群临时消息] ${member.remarkOrNick}(${member.id}) -> $messageString")
                bot.eventDispatcher.broadcastAsync(GroupTempMessageEvent(
                    member, miraiMessage, messageSource.time
                ))
            }
            "other" -> {
                val stranger = e.sender.wrapAsStranger(bot)

                if (stranger.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                    return
                }
                val message = e.toMiraiMessage(bot)
                val messageString = message.toString()
                val messageSource = IncomingSource.stranger(
                    bot = bot,
                    ids = intArrayOf(e.messageId),
                    internalIds = intArrayOf(e.messageId),
                    isOriginalMessageInitialized = true,
                    originalMessage = message,
                    sender = stranger,
                    subject = stranger,
                    target = bot,
                    time = (e.time / 1000).toInt()
                )
                val miraiMessage = messageSource.plus(message)
                bot.logger.verbose("${stranger.remarkOrNick}(${stranger.id}) -> $messageString")
                bot.eventDispatcher.broadcastAsync(StrangerMessageEvent(
                    stranger, miraiMessage, messageSource.time
                ))
            }
        }
    }
}

internal class FriendAddRequestListener : EventListener<FriendAddRequestEvent> {
    override suspend fun onMessage(e: FriendAddRequestEvent) {
        val bot = e.bot ?: return
        bot.eventDispatcher.broadcastAsync(NewFriendRequestEvent(
            bot = bot,
            eventId = Overflow.instance.putNewFriendRequestFlag(e.flag),
            message = e.comment,
            fromId = e.userId,
            fromGroupId = 0, // TODO: 获取来自哪个群
            fromNick = e.userId.takeIf { it > 0 }?.run { bot.queryProfile(this) { nickname } } ?: ""
        ))
    }
}

internal class FriendMessageRecallListener : EventListener<PrivateMsgDeleteNoticeEvent> {
    override suspend fun onMessage(e: PrivateMsgDeleteNoticeEvent) {
        val bot = e.bot ?: return
        val operatorId = e.operatorId.takeIf { it > 0 } ?: e.userId
        val friend = bot.getFriend(e.userId) ?: throw IllegalStateException("无法找到好友 ${e.userId}")
        bot.eventDispatcher.broadcastAsync(
            MessageRecallEvent.FriendRecall(bot,
            intArrayOf(e.msgId.toInt()),
            intArrayOf(e.msgId.toInt()),
            (e.time / 1000).toInt(),
            operatorId,
            friend
        ))
    }
}

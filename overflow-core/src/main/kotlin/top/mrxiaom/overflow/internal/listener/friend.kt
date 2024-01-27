@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evole.onebot.sdk.event.request.FriendAddRequestEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.contact.remarkOrNick
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.message.data.IncomingSource
import top.mrxiaom.overflow.internal.utils.queryProfile
import top.mrxiaom.overflow.internal.utils.wrapAsFriend
import top.mrxiaom.overflow.internal.utils.wrapAsStranger

internal fun EventBus.addFriendListeners(bot: BotWrapper) {
    listOf(
        FriendMessageListener(bot),
        FriendAddRequestListener(bot),

    ).forEach(::addListener)
}

internal class FriendMessageListener(
    val bot: BotWrapper
) : EventListener<PrivateMessageEvent> {
    override suspend fun onMessage(e: PrivateMessageEvent) {
        when (e.subType) {
            "friend" -> {
                val friend = e.privateSender.wrapAsFriend(bot)

                if (friend.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                    return
                }
                var miraiMessage = OnebotMessages.deserializeFromOneBot(bot, e.message)
                val messageString = miraiMessage.toString()
                val messageSource = IncomingSource.friend(
                    bot = bot,
                    ids = intArrayOf(e.messageId),
                    internalIds = intArrayOf(e.messageId),
                    isOriginalMessageInitialized = true,
                    originalMessage = miraiMessage,
                    sender = friend,
                    subject = friend,
                    target = bot,
                    time = (e.time / 1000).toInt()
                )
                miraiMessage = messageSource.plus(miraiMessage)
                bot.logger.verbose("${friend.remarkOrNick}(${friend.id}) -> $messageString")
                bot.eventDispatcher.broadcastAsync(FriendMessageEvent(
                    friend, miraiMessage, messageSource.time
                ))
            }
            "other", "group" -> { // TODO: group 群临时会话
                val stranger = e.privateSender.wrapAsStranger(bot)

                if (stranger.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                    return
                }
                var miraiMessage = OnebotMessages.deserializeFromOneBot(bot, e.message)
                val messageString = miraiMessage.toString()
                val messageSource = IncomingSource.stranger(
                    bot = bot,
                    ids = intArrayOf(e.messageId),
                    internalIds = intArrayOf(e.messageId),
                    isOriginalMessageInitialized = true,
                    originalMessage = miraiMessage,
                    sender = stranger,
                    subject = stranger,
                    target = bot,
                    time = (e.time / 1000).toInt()
                )
                miraiMessage = messageSource.plus(miraiMessage)
                bot.logger.verbose("${stranger.remarkOrNick}(${stranger.id}) -> $messageString")
                bot.eventDispatcher.broadcastAsync(StrangerMessageEvent(
                    stranger, miraiMessage, messageSource.time
                ))
            }
        }
    }
}

internal class FriendAddRequestListener(
    val bot: BotWrapper
): EventListener<FriendAddRequestEvent> {
    override suspend fun onMessage(e: FriendAddRequestEvent) {
        bot.eventDispatcher.broadcastAsync(NewFriendRequestEvent(
            bot = bot,
            eventId = Overflow.instance.putNewFriendRequestFlag(e.flag),
            message = e.comment ?: "",
            fromId = e.userId,
            fromGroupId = 0, // TODO: 获取来自哪个群
            fromNick = e.userId.takeIf { it > 0 }?.run { bot.queryProfile(this) { nickname } } ?: ""
        ))
    }
}

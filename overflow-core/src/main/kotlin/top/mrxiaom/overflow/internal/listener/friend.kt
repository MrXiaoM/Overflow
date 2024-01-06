@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.listener

import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evole.onebot.sdk.event.request.FriendAddRequestEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages
import top.mrxiaom.overflow.internal.utils.*

fun EventBus.addFriendListeners(bot: BotWrapper) {
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
                val messageSource = object : OnlineMessageSource.Incoming.FromFriend() {
                    override val bot: Bot = this@FriendMessageListener.bot
                    override val ids: IntArray = arrayOf(e.messageId).toIntArray()
                    override val internalIds: IntArray = ids
                    override val isOriginalMessageInitialized: Boolean = true
                    override val originalMessage: MessageChain = miraiMessage
                    override val sender: Friend = friend
                    override val subject: Friend = friend
                    override val target: ContactOrBot = bot
                    override val time: Int = (e.time / 1000).toInt()
                }
                miraiMessage = messageSource.plus(miraiMessage)
                bot.logger.verbose("${friend.remarkOrNick}(${friend.id}) -> $messageString")
                FriendMessageEvent(
                    friend, miraiMessage, messageSource.time
                ).broadcast()
            }
            "group" -> {
                TODO("群临时会话")
            }
            "other" -> {
                val stranger = e.privateSender.wrapAsStranger(bot)

                if (stranger.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                    return
                }
                var miraiMessage = OnebotMessages.deserializeFromOneBot(bot, e.message)
                val messageString = miraiMessage.toString()
                val messageSource = object : OnlineMessageSource.Incoming.FromStranger() {
                    override val bot: Bot = this@FriendMessageListener.bot
                    override val ids: IntArray = arrayOf(e.messageId).toIntArray()
                    override val internalIds: IntArray = ids
                    override val isOriginalMessageInitialized: Boolean = true
                    override val originalMessage: MessageChain = miraiMessage
                    override val sender: Stranger = stranger
                    override val subject: Stranger = stranger
                    override val target: ContactOrBot = bot
                    override val time: Int = (e.time / 1000).toInt()
                }
                miraiMessage = messageSource.plus(miraiMessage)
                bot.logger.verbose("${stranger.remarkOrNick}(${stranger.id}) -> $messageString")
                StrangerMessageEvent(
                    stranger, miraiMessage, messageSource.time
                ).broadcast()
            }
        }
    }
}

internal class FriendAddRequestListener(
    val bot: BotWrapper
): EventListener<FriendAddRequestEvent> {
    override suspend fun onMessage(e: FriendAddRequestEvent) {
        NewFriendRequestEvent(
            bot = bot,
            eventId = Overflow.instance.putNewFriendRequestFlag(e.flag),
            message = e.comment,
            fromId = e.userId,
            fromGroupId = 0, // TODO: 获取来自哪个群
            fromNick = e.userId.takeIf { it > 0 }?.run { bot.queryProfile(this) { nickname } } ?: ""
        ).broadcast()
    }
}

package top.mrxiaom.overflow.listener

import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.MessageSourceBuilder
import net.mamoe.mirai.message.data.MessageSourceKind
import top.mrxiaom.overflow.contact.BotWrapper
import top.mrxiaom.overflow.contact.FriendWrapper
import top.mrxiaom.overflow.message.OnebotMessages

internal class FriendMessageListener(
    val bot: BotWrapper
) : EventListener<PrivateMessageEvent> {
    override suspend fun onMessage(e: PrivateMessageEvent) {
        when (e.subType) {
            "friend" -> {
                val friend = bot.getFriendOrFail(e.userId)
                val messageSource = MessageSourceBuilder()
                    .id(e.messageId)
                    .internalId(e.messageId) // TODO: 协议内部ID
                    .time(e.time.toInt())
                    .sender(friend)
                    .target(bot)
                    .build(bot.id, MessageSourceKind.FRIEND)

                FriendMessageEvent(
                    friend,
                    OnebotMessages.deserializeFromOneBotJson(bot, e.message, messageSource),
                    e.time.toInt()
                ).broadcast()
            }
            "group" -> {
                TODO("群临时会话")
            }
            "other" -> {
                TODO("其它")
            }
        }
    }
}
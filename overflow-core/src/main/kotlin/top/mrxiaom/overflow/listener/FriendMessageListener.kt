package top.mrxiaom.overflow.listener

import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.contact.BotWrapper
import top.mrxiaom.overflow.contact.FriendWrapper
import top.mrxiaom.overflow.message.OnebotMessages

internal class FriendMessageListener(
    val bot: BotWrapper
) : EventListener<PrivateMessageEvent> {
    @OptIn(MiraiInternalApi::class)
    override suspend fun onMessage(e: PrivateMessageEvent) {
        when (e.subType) {
            "friend" -> {
                val friend = e.privateSender.wrapAsFriend(bot)

                if (friend.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                    return
                }
                var miraiMessage = OnebotMessages.deserializeFromOneBotJson(bot, e.message)
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
                    override val time: Int = e.time.toInt()
                }
                miraiMessage = messageSource.plus(miraiMessage)
                bot.logger.verbose("${friend.remarkOrNick}(${friend.id}) -> $messageString")
                FriendMessageEvent(
                    friend, miraiMessage, e.time.toInt()
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

fun PrivateMessageEvent.PrivateSender.wrapAsFriend(bot: BotWrapper): FriendWrapper {
    return FriendWrapper(bot, FriendInfoResp().also {
        it.userId = userId
        it.nickname = nickname
        it.remark = ""
    })
}
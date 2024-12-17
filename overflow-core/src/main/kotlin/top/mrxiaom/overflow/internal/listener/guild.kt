package top.mrxiaom.overflow.internal.listener

import cn.evolvefield.onebot.sdk.event.message.GuildMessageEvent
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import cn.evolvefield.onebot.sdk.entity.GuildSender
import net.mamoe.mirai.contact.MemberPermission
import top.mrxiaom.overflow.event.LegacyGuildMessageEvent
import top.mrxiaom.overflow.internal.message.OnebotMessages.toMiraiMessage
import top.mrxiaom.overflow.internal.utils.bot

internal fun addGuildListeners() {
    listOf(
        GuildMessageListener(),

    ).forEach(EventBus::addListener)
}

internal class GuildMessageListener : EventListener<GuildMessageEvent> {
    override suspend fun onMessage(e: GuildMessageEvent) {
        val bot = e.bot ?: return
        when (e.subType) {
            "channel" -> {
                val message = e.toMiraiMessage(bot)
                val messageString = message.toString()

                if (e.sender.userId == bot.id) {
                    // TODO: 过滤自己发送的消息
                } else {
                    bot.logger.verbose("[频道][${e.guildId}(${e.channelId})] ${e.sender.nameCardOrNick}(${e.sender.userId}) -> $messageString")
                    bot.eventDispatcher.broadcastAsync(LegacyGuildMessageEvent(
                        bot = bot,
                        guildId = e.guildId,
                        channelId = e.channelId,
                        messageId = e.messageId,
                        message = message,
                        senderId = e.sender.userId,
                        senderTinyId = e.sender.tinyId,
                        senderNick = e.sender.nickname,
                        senderNameCard = e.sender.card,
                        senderTitle = e.sender.title,
                        senderLevel = e.sender.level,
                        senderRole = when(e.sender.role.lowercase()) {
                            "owner" -> MemberPermission.OWNER
                            "admin" -> MemberPermission.ADMINISTRATOR
                            else -> MemberPermission.MEMBER
                        },
                        time = e.timeInSecond().toInt()
                    ))
                }
            }
        }
    }

    private val GuildSender.nameCardOrNick: String
        get() = card.takeIf { it.isNotBlank() } ?: nickname
}

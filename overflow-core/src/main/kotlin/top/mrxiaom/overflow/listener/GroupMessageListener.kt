package top.mrxiaom.overflow.listener

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.client.core.Bot
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.getMemberOrFail
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.MessageSourceBuilder
import net.mamoe.mirai.message.data.MessageSourceKind
import top.mrxiaom.overflow.contact.BotWrapper
import top.mrxiaom.overflow.contact.GroupWrapper
import top.mrxiaom.overflow.contact.MemberWrapper
import top.mrxiaom.overflow.message.OnebotMessages

internal class GroupMessageListener(
    val bot: BotWrapper
) : EventListener<GroupMessageEvent> {
    override suspend fun onMessage(e: GroupMessageEvent) {
        when(e.subType) {
            "normal" -> {
                val group = bot.getGroupOrFail(e.groupId)
                val member = group.getMemberOrFail(e.userId)
                val messageSource = MessageSourceBuilder()
                    .id(e.messageId)
                    .internalId(e.messageId) // TODO: 协议内部ID
                    .time(e.time.toInt())
                    .sender(member)
                    .target(bot)
                    .build(bot.id, MessageSourceKind.GROUP)

                net.mamoe.mirai.event.events.GroupMessageEvent(
                    e.sender.card, when (e.sender.role) {
                        "owner" -> MemberPermission.OWNER
                        "admin" -> MemberPermission.ADMINISTRATOR
                        else -> MemberPermission.MEMBER
                    }, member,
                    OnebotMessages.deserializeFromOneBotJson(bot, e.message, messageSource),
                    e.time.toInt()
                ).broadcast()
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
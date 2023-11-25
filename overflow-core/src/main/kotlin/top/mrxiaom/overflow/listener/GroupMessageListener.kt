package top.mrxiaom.overflow.listener

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evole.onebot.sdk.event.message.GroupMessageEvent.GroupSender
import cn.evole.onebot.sdk.response.group.GroupMemberInfoResp
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.asOnebot
import top.mrxiaom.overflow.contact.BotWrapper
import top.mrxiaom.overflow.contact.GroupWrapper
import top.mrxiaom.overflow.contact.MemberWrapper
import top.mrxiaom.overflow.message.OnebotMessages

internal class GroupMessageListener(
    val bot: BotWrapper
) : EventListener<GroupMessageEvent> {
    @OptIn(MiraiInternalApi::class)
    override suspend fun onMessage(e: GroupMessageEvent) {
        when(e.subType) {
            "normal" -> {
                val group = bot.getGroupOrFail(e.groupId)
                val member = e.sender.wrapAsMember(group)

                var miraiMessage = OnebotMessages.deserializeFromOneBotJson(bot, e.message)
                val messageString = miraiMessage.toString()
                val messageSource = object : OnlineMessageSource.Incoming.FromGroup() {
                    override val bot: Bot = this@GroupMessageListener.bot
                    override val ids: IntArray = arrayOf(e.messageId).toIntArray()
                    override val internalIds: IntArray = ids
                    override val isOriginalMessageInitialized: Boolean = true
                    override val originalMessage: MessageChain = miraiMessage
                    override val sender: Member = member
                    override val time: Int = e.time.toInt()
                }
                miraiMessage = messageSource.plus(miraiMessage)
                val logger = bot.configuration.botLoggerSupplier(bot)
                if (member.id == bot.id) {
                    // TODO: 过滤自己发送的消息
                } else {
                    logger.info("[${group.name}(${group.id})] ${member.nameCardOrNick}(${member.id}) -> $messageString")
                    net.mamoe.mirai.event.events.GroupMessageEvent(
                        e.sender.card, when (e.sender.role) {
                            "owner" -> MemberPermission.OWNER
                            "admin" -> MemberPermission.ADMINISTRATOR
                            else -> MemberPermission.MEMBER
                        }, member, miraiMessage, e.time.toInt()
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

fun GroupSender.wrapAsMember(group: Group): MemberWrapper {
    return MemberWrapper(group.bot.asOnebot, group as GroupWrapper, GroupMemberInfoResp().also {
        it.groupId = group.id
        it.userId = userId.toLong()
        it.nickname = nickname
        it.card = card
        it.sex = sex
        it.age = age
        it.area = area
        it.level = level.toIntOrNull() ?: 0
        it.role = role
        it.title = title
    })
}

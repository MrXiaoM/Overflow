package top.mrxiaom.overflow.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.events.BotEvent

public class MessageReactionEvent(
    /**
     * 机器人
     */
    public override val bot: Bot,
    /**
     * 群聊
     */
    public val group: Group,
    /**
     * 操作人，null 时为机器人
     */
    public val operator: NormalMember?,
    /**
     * 被操作的消息ID
     */
    public val messageId: Int,
    /**
     * 表情ID，详见 Overflow 开发文档
     */
    public val reaction: String,
    /**
     * 进行此操作后的表情总数
     */
    public val count: Int,
    /**
     * 表情被添加还是移除
     *
     * `true` 为添加，`false` 为移除
     */
    public val operation: Boolean,
): BotEvent, AbstractEvent()

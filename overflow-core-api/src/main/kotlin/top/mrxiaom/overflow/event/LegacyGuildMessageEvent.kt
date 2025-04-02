package top.mrxiaom.overflow.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.message.data.MessageChain

/**
 * 临时的频道消息事件
 *
 * 在正式确定频道接口之前，可使用此事件接收频道消息
 */
@Deprecated("腾讯自己都不积极维护频道了，没必要支持了")
public class LegacyGuildMessageEvent(
    /**
     * 接收消息的机器人
     */
    public val bot: Bot,
    /**
     * 频道ID
     */
    public val guildId: String,
    /**
     * 子频道ID
     */
    public val channelId: String,
    /**
     * 消息ID
     */
    public val messageId: String,
    /**
     * 收到的消息
     */
    public val message: MessageChain,
    /**
     * 消息发送者ID
     */
    public val senderId: Long,
    /**
     * 消息发送者TinyID
     */
    public val senderTinyId: String,
    /**
     * 消息发送者昵称
     */
    public val senderNick: String,
    /**
     * 消息发送者名片
     */
    public val senderNameCard: String,
    /**
     * 消息发送者头衔
     */
    public val senderTitle: String,
    /**
     * 消息发送者等级
     */
    public val senderLevel: String,
    /**
     * 消息发送者权限
     */
    public val senderRole: MemberPermission,
    /**
     * 消息发送时间
     */
    public val time: Int
): AbstractEvent() {
    public override fun toString(): String =
        "LegacyGuildMessageEvent(guildId=$guildId, channelId=$channelId, senderNameCard=$senderNameCard, sender=${senderId}, permission=${senderRole}, message=$message)"
}
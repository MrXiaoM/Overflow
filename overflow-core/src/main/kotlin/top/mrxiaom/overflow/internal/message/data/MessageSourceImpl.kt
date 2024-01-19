@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OfflineMessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.utils.MiraiInternalApi

internal class OfflineMessageSourceImpl(
    override val botId: Long,
    override val fromId: Long,
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val isOriginalMessageInitialized: Boolean,
    override val originalMessage: MessageChain,
    override val targetId: Long,
    override val time: Int,
    override val kind: MessageSourceKind
) : OfflineMessageSource()

internal object OutgoingSource {
    internal fun group(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Bot,
        target: Group,
        time: Int
    ): OnlineMessageSource.Outgoing.ToGroup {
        return object : OnlineMessageSource.Outgoing.ToGroup() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Bot = sender
            override val target: Group = target
            override val time: Int = time
        }
    }
    internal fun friend(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Bot,
        target: Friend,
        time: Int
    ): OnlineMessageSource.Outgoing.ToFriend {
        return object : OnlineMessageSource.Outgoing.ToFriend() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Bot = sender
            override val target: Friend = target
            override val time: Int = time
        }
    }
    internal fun temp(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Bot,
        target: Member,
        time: Int
    ): OnlineMessageSource.Outgoing.ToTemp {
        return object : OnlineMessageSource.Outgoing.ToTemp() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Bot = sender
            override val target: Member = target
            override val time: Int = time
        }
    }
    internal fun stranger(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Bot,
        target: Stranger,
        time: Int
    ): OnlineMessageSource.Outgoing.ToStranger {
        return object : OnlineMessageSource.Outgoing.ToStranger() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Bot = sender
            override val target: Stranger = target
            override val time: Int = time
        }
    }
    internal fun <C : Contact> OnlineMessageSource.Outgoing.receipt(contact: C): MessageReceipt<C> {
        @Suppress("DEPRECATION_ERROR")
        return MessageReceipt(this, contact)
    }
}

internal object IncomingSource {
    internal fun group(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Member,
        time: Int
    ): OnlineMessageSource.Incoming.FromGroup {
        return object : OnlineMessageSource.Incoming.FromGroup() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Member = sender
            override val time: Int = time
        }
    }
    internal fun friend(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Friend,
        subject: Friend,
        target: ContactOrBot,
        time: Int
    ): OnlineMessageSource.Incoming.FromFriend {
        return object : OnlineMessageSource.Incoming.FromFriend() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Friend = sender
            override val subject: Friend = subject
            override val target: ContactOrBot = target
            override val time: Int = time
        }
    }
    internal fun temp(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Member,
        subject: Member,
        target: ContactOrBot,
        time: Int
    ): OnlineMessageSource.Incoming.FromTemp {
        return object : OnlineMessageSource.Incoming.FromTemp() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Member = sender
            override val subject: Member = subject
            override val target: ContactOrBot = target
            override val time: Int = time
        }
    }
    internal fun stranger(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Stranger,
        subject: Stranger,
        target: ContactOrBot,
        time: Int
    ): OnlineMessageSource.Incoming.FromStranger {
        return object : OnlineMessageSource.Incoming.FromStranger() {
            override val bot: Bot = bot
            override val ids: IntArray = ids
            override val internalIds: IntArray = internalIds
            override val isOriginalMessageInitialized: Boolean = isOriginalMessageInitialized
            override val originalMessage: MessageChain = originalMessage
            override val sender: Stranger = sender
            override val subject: Stranger = subject
            override val target: ContactOrBot = target
            override val time: Int = time
        }
    }
}

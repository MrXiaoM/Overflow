@file:OptIn(MiraiInternalApi::class)
@file:Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.internal.message.MessageSourceSerializerImpl
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi

@OptIn(MiraiExperimentalApi::class)
internal fun MessageSerializers.registerMessageSourceSerializers() {
    registerSerializer(MessageSource::class, MessageSource.serializer())
    registerSerializer(OfflineMessageSourceImpl::class, OfflineMessageSourceImpl.serializer())
    registerSerializer(OutgoingSource.ToGroup::class, OutgoingSource.ToGroup.serializer())
    registerSerializer(OutgoingSource.ToFriend::class, OutgoingSource.ToFriend.serializer())
    registerSerializer(OutgoingSource.ToTemp::class, OutgoingSource.ToTemp.serializer())
    registerSerializer(OutgoingSource.ToStranger::class, OutgoingSource.ToStranger.serializer())
    registerSerializer(IncomingSource.FromGroup::class, IncomingSource.FromGroup.serializer())
    registerSerializer(IncomingSource.FromFriend::class, IncomingSource.FromFriend.serializer())
    registerSerializer(IncomingSource.FromTemp::class, IncomingSource.FromTemp.serializer())
    registerSerializer(IncomingSource.FromStranger::class, IncomingSource.FromStranger.serializer())
}

@Serializable(OfflineMessageSourceImpl.Serializer::class)
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
) : OfflineMessageSource() {
    object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OfflineMessageSource")
}

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
    ): ToGroup = ToGroup(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, target, time)
    internal fun friend(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Bot,
        target: Friend,
        time: Int
    ): ToFriend = ToFriend(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, target, time)
    internal fun temp(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Bot,
        target: Member,
        time: Int
    ): ToTemp = ToTemp(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, target, time)
    internal fun stranger(
        bot: Bot,
        ids: IntArray,
        internalIds: IntArray,
        isOriginalMessageInitialized: Boolean,
        originalMessage: MessageChain,
        sender: Bot,
        target: Stranger,
        time: Int
    ): ToStranger = ToStranger(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, target, time)
    internal fun <C : Contact> OnlineMessageSource.Outgoing.receipt(contact: C): MessageReceipt<C> {
        @Suppress("DEPRECATION_ERROR")
        return MessageReceipt(this, contact)
    }
    @Serializable(ToGroup.Serializer::class)
    class ToGroup(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Bot,
        override val target: Group,
        override val time: Int,
    ): OnlineMessageSource.Outgoing.ToGroup() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceToGroup")
    }
    @Serializable(ToFriend.Serializer::class)
    class ToFriend(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Bot,
        override val target: Friend,
        override val time: Int,
    ): OnlineMessageSource.Outgoing.ToFriend() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceToFriend")
    }
    @Serializable(ToTemp.Serializer::class)
    class ToTemp(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Bot,
        override val target: Member,
        override val time: Int,
    ): OnlineMessageSource.Outgoing.ToTemp() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceToTemp")
    }
    @Serializable(ToStranger.Serializer::class)
    class ToStranger(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Bot,
        override val target: Stranger,
        override val time: Int,
    ): OnlineMessageSource.Outgoing.ToStranger() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceToStranger")
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
    ): FromGroup = FromGroup(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, time)
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
    ): FromFriend = FromFriend(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, subject, target, time)
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
    ): FromTemp = FromTemp(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, subject, target, time)
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
    ): FromStranger = FromStranger(bot, ids, internalIds, isOriginalMessageInitialized, originalMessage, sender, subject, target, time)

    @Serializable(FromGroup.Serializer::class)
    class FromGroup(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Member,
        override val time: Int,
    ): OnlineMessageSource.Incoming.FromGroup() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromGroup")
    }
    @Serializable(FromFriend.Serializer::class)
    class FromFriend(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Friend,
        override val subject: Friend,
        override val target: ContactOrBot,
        override val time: Int,
    ): OnlineMessageSource.Incoming.FromFriend() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromFriend")
    }
    @Serializable(FromTemp.Serializer::class)
    class FromTemp(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Member,
        override val subject: Member,
        override val target: ContactOrBot,
        override val time: Int,
    ): OnlineMessageSource.Incoming.FromTemp() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromTemp")
    }
    @Serializable(FromStranger.Serializer::class)
    class FromStranger(
        override val bot: Bot,
        override val ids: IntArray,
        override val internalIds: IntArray,
        override val isOriginalMessageInitialized: Boolean,
        override val originalMessage: MessageChain,
        override val sender: Stranger,
        override val subject: Stranger,
        override val target: ContactOrBot,
        override val time: Int,
    ): OnlineMessageSource.Incoming.FromStranger() {
        object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("OnlineMessageSourceFromStranger")
    }
}

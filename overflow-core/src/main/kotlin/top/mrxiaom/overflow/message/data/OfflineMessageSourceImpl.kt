package top.mrxiaom.overflow.message.data

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OfflineMessageSource

class OfflineMessageSourceImpl(
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

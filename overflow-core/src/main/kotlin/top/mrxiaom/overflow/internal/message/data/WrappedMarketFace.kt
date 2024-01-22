package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast

@MiraiExperimentalApi
@Serializable
@SerialName(WrappedMarketFace.SERIAL_NAME)
internal data class WrappedMarketFace(
    val emojiId: String,
    override val name: String,
    override val id: Int = 0
): MarketFace {
    override fun toString(): String {
        return "[overflow:MarketFace,$emojiId]"
    }

    override val key: MessageKey<WrappedMarketFace> get() = Key
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, WrappedMarketFace>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "WrappedMarketFace"
    }
}

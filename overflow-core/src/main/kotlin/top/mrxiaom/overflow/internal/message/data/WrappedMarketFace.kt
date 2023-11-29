package top.mrxiaom.overflow.internal.message.data

import net.mamoe.mirai.message.data.MarketFace
import net.mamoe.mirai.utils.MiraiExperimentalApi

@MiraiExperimentalApi
class WrappedMarketFace(
    val emojiId: String,
    override val name: String,
    override val id: Int = 0
): MarketFace {
    override fun toString(): String {
        return "[overflow:MarketFace,$emojiId]"
    }
}

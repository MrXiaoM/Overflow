package net.mamoe.mirai.internal.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.MarketFace
import net.mamoe.mirai.utils.MiraiExperimentalApi

@SerialName(MarketFaceImpl.SERIAL_NAME)
@Serializable
internal data class MarketFaceImpl internal constructor(
    internal val delegate: ImMsgBody.MarketFace,
) : MarketFace {

    override val name: String get() = delegate.faceName.decodeToString()

    @Transient
    @MiraiExperimentalApi
    override val id: Int = delegate.tabId

    @OptIn(MiraiExperimentalApi::class)
    override fun toString() = "[mirai:marketface:$id,$name]"

    companion object {
        const val SERIAL_NAME = MarketFace.SERIAL_NAME
    }
}
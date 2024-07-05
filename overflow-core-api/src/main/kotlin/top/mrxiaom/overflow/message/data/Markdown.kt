@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package top.mrxiaom.overflow.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendStringAsMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast

/**
 * Markdown消息. 用于官方 Bot
 *
 * 使用时直接构造即可.
 */
@Serializable
@SerialName(Markdown.SERIAL_NAME)
public data class Markdown(
    /**
     * Markdown 内容
     */
    public val content: String
) : MessageContent, CodableMessage {
    public override fun toString(): String = "[overflow:markdown,$content]"
    public override fun contentToString(): String = content

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:markdown:").appendStringAsMiraiCode(content).append("]")
    }

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, Markdown>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "Markdown"
    }
}
/**
 * 构造 [Markdown]
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
public inline fun String.toMarkdown(): Markdown = Markdown(this)

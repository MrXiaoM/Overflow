@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package top.mrxiaom.overflow.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendStringAsMiraiCode
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 推荐联系人
 */
@Serializable
@SerialName(ContactRecommend.SERIAL_NAME)
public data class ContactRecommend(
    /**
     * 联系人类型
     */
    public val type: ContactType,
    public val id: Long
) : MessageContent, CodableMessage {
    private val _contentValue: String by lazy(LazyThreadSafetyMode.NONE) {
        val typeString = when(type) {
            ContactType.Group -> "群聊"
            ContactType.Private -> "好友"
        }
        "[推荐$typeString]$id"
    }
    public override fun toString(): String = "[overflow:contact,$type,$id]"
    public override fun contentToString(): String = _contentValue

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:contact:").append(type.name.lowercase()).append(",").append(id).append("]")
    }

    /**
     * 推荐联系人类型
     */
    public enum class ContactType {
        /**
         * 群聊
         */
        Group,

        /**
         * 私聊
         */
        Private
    }
    public companion object {
        public const val SERIAL_NAME: String = "Contact"
    }
}

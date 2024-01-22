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
 * 推荐联系人
 */
@Serializable
@SerialName(ContactRecommend.SERIAL_NAME)
public data class ContactRecommend(
    /**
     * 联系人类型
     */
    public val contactType: ContactType,
    public val id: Long
) : MessageContent, ConstrainSingle, CodableMessage {
    private val _contentValue: String by lazy(LazyThreadSafetyMode.NONE) {
        val typeString = when(contactType) {
            ContactType.Group -> "群聊"
            ContactType.Private -> "好友"
        }
        "[推荐$typeString]$id"
    }
    public override fun toString(): String = "[overflow:contact,$contactType,$id]"
    public override fun contentToString(): String = _contentValue

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:contact:").append(contactType.name.lowercase()).append(",").append(id).append("]")
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

    override val key: MessageKey<ContactRecommend> get() = Key
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, ContactRecommend>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "Contact"
    }
}

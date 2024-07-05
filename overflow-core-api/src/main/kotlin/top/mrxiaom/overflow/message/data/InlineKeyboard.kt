package top.mrxiaom.overflow.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast

/**
 * 内联键盘消息. 常见于 Markdown 消息下方
 */
@Serializable
@SerialName(InlineKeyboard.SERIAL_NAME)
public data class InlineKeyboard(
    /**
     * 官方机器人 appId
     */
    public val botAppId: Long,
    /**
     * 按键行列表
     */
    public val rows: List<InlineKeyboardRow>
) : MessageContent {
    private val string by lazy {
        "[overflow:inlinekeyboard,$botAppId,${rows.joinToString(",")}]"
    }
    private val content by lazy {
        "\n[内联键盘]\n" +
        rows.joinToString("\n") { row -> row.buttons.joinToString(", ") { it.label } }
    }
    public override fun toString(): String = string
    public override fun contentToString(): String = content

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, InlineKeyboard>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "InlineKeyboard"
    }
}

/**
 * 内联键盘按键行
 */
@Serializable
public data class InlineKeyboardRow(
    /**
     * 这一行的按钮列表
     */
    val buttons: List<InlineKeyboardButton>
) {
    private val stringContent by lazy {
        "{row:[${buttons.joinToString(",")}]}"
    }
    override fun toString(): String = stringContent
}
/**
 * 内联键盘按钮
 */
@Serializable
public data class InlineKeyboardButton(
    val id: String,
    val label: String,
    val visitedLabel: String,
    val style: Int,
    val type: Int,
    val clickLimit: Int,
    val unsupportTips: String,
    val data: String,
    val atBotShowChannelList: Boolean,
    val permissionType: Int,
    val specifyRoleIds: List<String>,
    val specifyTinyIds: List<String>
) {
    override fun toString(): String {
        return "[button:$label]"
    }
}

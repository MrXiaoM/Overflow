package top.mrxiaom.overflow

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiLogger
import top.mrxiaom.overflow.contact.RemoteBot

val Overflow: OverflowAPI
    get() = OverflowAPI.get()
interface OverflowAPI {
    /**
     * Overflow 版本号
     */
    val version: String
    val botStarter: IBotStarter
    /**
     * 以 Onebot 格式新建图片消息
     *
     * 另请参见 [Onebot file 参数说明](https://github.com/botuniverse/onebot-11/blob/master/message/segment.md#%E5%9B%BE%E7%89%87)
     */
    fun imageFromFile(file: String): Image
    /**
     * 以 Onebot 格式新建语音消息
     *
     * 另请参见 [Onebot file 参数说明](https://github.com/botuniverse/onebot-11/blob/master/message/segment.md#%E5%9B%BE%E7%89%87)
     */
    fun audioFromFile(file: String): Audio
    /**
     * 以 Onebot 格式新建短视频消息
     *
     * 另请参见 [Onebot file 参数说明](https://github.com/botuniverse/onebot-11/blob/master/message/segment.md#%E5%9B%BE%E7%89%87)
     */
    fun videoFromFile(file: String): ShortVideo

    /**
     * 序列化 mirai 格式消息为 Onebot 格式 json 消息段
     */
    @Deprecated(
        message = "Please use serializeMessage(bot, message)",
        ReplaceWith("serializeMessage(bot, message)")
    )
    fun serializeMessage(message: Message): String = serializeMessage(null, message)

    /**
     * 序列化 mirai 格式消息为 Onebot 格式 json 消息段
     */
    fun serializeMessage(bot: RemoteBot?, message: Message): String

    /**
     * 序列化 Onebot 格式 json 消息段为 CQ 码
     */
    fun serializeJsonToCQCode(messageJson: String): String

    /**
     * 序列化 Onebot 格式 CQ 码 为 json 消息段
     */
    fun serializeCQCodeToJson(messageCQCode: String): String

    /**
     * 反序列化 Onebot 格式 json 消息段或 CQ 码，反序列化为 mirai 格式消息
     */
    @JvmBlockingBridge
    suspend fun deserializeMessage(bot: Bot, message: String): MessageChain

    /**
     * 反序列化 json 消息段为 mirai 格式消息
     */
    @JvmBlockingBridge
    suspend fun deserializeMessageFromJson(bot: Bot, message: String): MessageChain?

    /**
     * 反序列化 CQ 码 为 mirai 格式消息
     */
    @JvmBlockingBridge
    suspend fun deserializeMessageFromCQCode(bot: Bot, message: String): MessageChain?

    companion object {
        public val logger = MiraiLogger.Factory.create(OverflowAPI::class, "Overflow")
        @JvmStatic
        fun get(): OverflowAPI = Mirai as OverflowAPI
    }
}

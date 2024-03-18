package top.mrxiaom.overflow

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiLogger
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.message.MessageProcessor

val Overflow: OverflowAPI
    get() = OverflowAPI.get()
interface OverflowAPI {
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
     * 序列化 mirai 格式消息为 Onebot 格式 json 数组消息
     */
    @Deprecated(
        message = "Please use serializeMessage(bot, message)",
        ReplaceWith("serializeMessage(bot, message)")
    )
    fun serializeMessage(message: Message): String = serializeMessage(null, message)

    /**
     * 序列化 mirai 格式消息为 Onebot 格式 json 数组消息
     */
    fun serializeMessage(bot: RemoteBot?, message: Message): String

    /**
     * 获取消息处理器列表
     */
    val messageProcessors: List<MessageProcessor<*>>

    /**
     * 注册消息处理器
     *
     * 如果要添加一种新的消息类型，除了编写并注册消息处理器外，还需要将该消息类型注册到 [net.mamoe.mirai.message.MessageSerializers]
     */
    fun registerMessageProcessor(processor: MessageProcessor<*>)

    /**
     * 反序列化 Onebot 格式 json 数组消息为 mirai 格式消息
     */
    @JvmBlockingBridge
    suspend fun deserializeMessage(bot: Bot, message: String): MessageChain

    companion object {
        public val logger = MiraiLogger.Factory.create(OverflowAPI::class, "Overflow")
        @JvmStatic
        fun get(): OverflowAPI = Mirai as OverflowAPI
    }
}

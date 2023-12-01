package top.mrxiaom.overflow

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*

interface OverflowAPI {
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
    fun serializeMessage(message: Message): String
    /**
     * 反序列化 Onebot 格式 json 数组消息为 mirai 格式消息
     */
    @JvmBlockingBridge
    suspend fun deserializeMessage(bot: Bot, message: String): MessageChain

    companion object {
        @JvmStatic
        fun get(): OverflowAPI = Mirai as OverflowAPI
    }
}

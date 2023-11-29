package top.mrxiaom.overflow.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import kotlin.jvm.Throws

interface RemoteBot {
    /**
     * 执行自定义动作
     *
     * 详见 [Onebot 11 公开 API](https://github.com/botuniverse/onebot-11/blob/master/api/public.md)。
     * @param path API请求路径
     * @param json 请求参数，可为空
     * @return 服务端返回信息，格式为 json
     */
    @JvmBlockingBridge
    suspend fun executeAction(path: String, json: String?): String

    companion object {
        /**
         * 尝试将 mirai Bot 转换为 Overflow RemoteBot
         *
         * 当类型不正确时，或没有 Overflow 实现时，将会抛出异常
         */
        @JvmStatic
        @get:Throws(ClassNotFoundException::class, ClassCastException::class)
        val Bot.asRemoteBot
            get() = this as RemoteBot
    }
}

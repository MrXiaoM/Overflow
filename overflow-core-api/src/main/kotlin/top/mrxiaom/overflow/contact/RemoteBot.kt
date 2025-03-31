package top.mrxiaom.overflow.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.MessageChain
import top.mrxiaom.overflow.action.ActionContext
import kotlin.jvm.Throws

interface RemoteBot {
    /**
     * Onebot 实现名称
     */
    val appName: String

    /**
     * Onebot 实现版本
     */
    val appVersion: String

    /**
     * 配置中是否已禁止 Overflow 调用网络接口
     */
    val noPlatform: Boolean

    /**
     * 执行自定义动作
     *
     * 详见 [Onebot 11 公开 API](https://github.com/botuniverse/onebot-11/blob/master/api/public.md)。
     * @param actionPath API请求路径
     * @param params 请求参数，可为空
     * @return 服务端返回信息，格式为 json
     */
    @JvmBlockingBridge
    suspend fun executeAction(actionPath: String, params: String?): String

    /**
     * 执行自定义动作
     *
     * 详见 [Onebot 11 公开 API](https://github.com/botuniverse/onebot-11/blob/master/api/public.md) 和 [go-cqhttp 公开 API](https://docs.go-cqhttp.org/api)。
     *
     * 用例:
     * ```kotlin
     * // kotlin
     * val result = onebot.executeAction(
     *     context = ActionContext.build("get_guild_list") {
     *         throwExceptions(true)
     *     },
     *     params = null
     * )
     * ```
     * ```java
     * // java
     * String result = onebot.executeAction(
     *     ActionContext.build("get_guild_list", builder -> {
     *         builder.throwExceptions(true);
     *     }),
     *     null
     * );
     * ```
     *
     * @param context 自定义请求上下文，可控制抛出异常、忽略标准格式等选项
     * @param params 请求参数，可为空
     * @return 服务端返回信息，格式为 json
     */
    @JvmBlockingBridge
    suspend fun executeAction(context: ActionContext, params: String?): String

    /**
     * 发送自定义 WebSocket 消息
     * @param message 消息内容
     */
    fun sendRawWebSocketMessage(message: String)

    /**
     * 发送自定义 WebSocket 消息
     * @param message 消息内容
     */
    fun sendRawWebSocketMessage(message: ByteArray)

    /**
     * 通过消息id获取消息，并反序列化为 mirai 格式消息
     * @param messageId 消息id
     */
    @JvmBlockingBridge
    suspend fun getMsg(messageId: Int): MessageChain?

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

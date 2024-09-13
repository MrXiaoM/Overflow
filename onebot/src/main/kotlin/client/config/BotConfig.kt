package cn.evolvefield.onebot.client.config

import kotlinx.coroutines.Job

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/1 17:05
 * Version: 1.0
 */
class BotConfig(
    /**
     * websocket地址
     */
    val url: String = "ws://127.0.0.1:8080",
    /**
     * 反向 websocket 端口
     */
    val reversedPort: Int = -1,
    /**
     * token或者verifyKey鉴权
     */
    val token: String = "",
    /**
     * 是否开启鉴权
     */
    val isAccessToken: Boolean = false,
    /**
     * 是否禁止 Overflow 通过 get_credentials 获取凭证调用网络接口
     *
     * 该选项无法禁止其它插件自行调用网络接口
     */
    val noPlatform: Boolean = false,
    /**
     * 发送消息时，是否使用 CQ 码
     */
    val useCQCode: Boolean = false,
    /**
     * 重连尝试次数
     */
    val retryTimes: Int = 5,
    /**
     * 重连间隔 (毫秒)
     */
    val retryWaitMills: Long = 5000L,
    /**
     * 重连休息时间 (毫秒)
     */
    val retryRestMills: Long = 60000L,
    val parentJob: Job? = null
) {
    val isInReverseMode get() = reversedPort in 1..65535
}
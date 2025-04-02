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
    val retryWaitMills: Long = 5_000L,
    /**
     * 重连休息时间 (毫秒)
     */
    val retryRestMills: Long = 60_000L,
    /**
     * 心跳包检测时间 (秒)，设为0关闭检测
     */
    val heartbeatCheckSeconds: Int = 60,
    /**
     * 是否不接收群聊的 file 消息，使用 group_upload 事件作为群文件消息
     */
    val useGroupUploadEventForFileMessage: Boolean = false,
    /**
     * 是否抛弃所有在Bot成功连接之前传入的事件。
     *
     * 比如 go-cqhttp 会在连接之后，在 Overflow 获取到 Bot 信息之前，推送以前未收到的消息。如果开启此项，则丢弃这些消息
     */
    val dropEventsBeforeConnected: Boolean = true,
    val parentJob: Job? = null
) {
    val isInReverseMode get() = reversedPort in 1..65535
}
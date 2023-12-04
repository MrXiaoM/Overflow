package cn.evolvefield.onebot.client.config

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
     * token或者verifyKey鉴权
     */
    val token: String = "",

    val botId: Long = 0,
    /**
     * 是否开启鉴权
     */
    val isAccessToken: Boolean = false,
    /**
     * 是否开启mirai-http,否则请使用onebot-mirai
     */
    val miraiHttp: Boolean = false,
    /**
     * 是否开启重连
     */
    val reconnect: Boolean = true,
    /**
     * 重连间隔
     */
    val maxReconnectAttempts: Int = 20,
)

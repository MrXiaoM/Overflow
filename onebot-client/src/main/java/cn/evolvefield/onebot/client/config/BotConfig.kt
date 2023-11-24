package cn.evolvefield.onebot.client.config

import com.google.gson.annotations.Expose
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/1 17:05
 * Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class BotConfig(
    /**
     * websocket地址
     */
    @Expose
    val url: String = "ws://127.0.0.1:8080",
    /**
     * token或者verifyKey鉴权
     */
    @Expose
    val token: String = "",

    @Expose
    val botId: Long = 0,
    /**
     * 是否开启鉴权
     */
    @Expose
    val isAccessToken: Boolean = false,
    /**
     * 是否开启mirai-http,否则请使用onebot-mirai
     */
    @Expose
    val miraiHttp: Boolean = false,
    /**
     * 是否开启重连
     */
    @Expose
    val reconnect: Boolean = true,
    /**
     * 重连间隔
     */
    @Expose
    val maxReconnectAttempts: Int = 20,
) {
    constructor(url: String) : this(url, "", 0, false, false, true, 20)
    constructor(url: String, botId: Long) : this(url, "", botId, false, true, true, 20)
}
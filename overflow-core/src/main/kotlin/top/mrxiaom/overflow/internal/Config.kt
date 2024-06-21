package top.mrxiaom.overflow.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("ws_host")
    var wsHost: String = "ws://127.0.0.1:3001",
    @SerialName("reversed_ws_port")
    var reversedWSPort: Int = -1,
    @SerialName("token")
    var token: String = "",
    @SerialName("no_platform")
    val noPlatform: Boolean = false,
    @SerialName("retry_times")
    var retryTimes: Int = 5,
    @SerialName("retry_wait_mills")
    var retryWaitMills: Long = 5_000L,
    @SerialName("retry_rest_mills")
    var retryRestMills: Long = 60_000L,
)
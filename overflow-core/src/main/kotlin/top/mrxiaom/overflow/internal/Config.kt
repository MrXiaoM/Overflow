package top.mrxiaom.overflow.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("ws_host")
    var wsHost: String = "ws://127.0.0.1:5800",
    @SerialName("reversed_ws_port")
    var reversedWSPort: Int = -1,
    @SerialName("token")
    var token: String = ""
)
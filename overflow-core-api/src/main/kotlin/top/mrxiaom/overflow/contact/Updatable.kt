package top.mrxiaom.overflow.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge

interface Updatable {
    /**
     * 请求更新信息
     */
    @JvmBlockingBridge
    suspend fun queryUpdate()
}

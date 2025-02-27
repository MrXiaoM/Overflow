/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package net.mamoe.mirai.internal.message

import net.mamoe.mirai.message.data.*

internal sealed class MessageRefiner

/**
 * 执行不需要 `suspend` 的 refine. 用于 [MessageSource.originalMessage].
 *
 * 兼容 cssxsh/mirai-hibernate-plugin
 *
 * https://github.com/cssxsh/mirai-hibernate-plugin/blob/8f425db01629ff84900b53b1bf86c741ef7f81be/src/main/kotlin/xyz/cssxsh/mirai/hibernate/MiraiHibernateRecorder.kt
 */
@Suppress("unused")
internal object LightMessageRefiner : MessageRefiner() {
    /**
     * 去除 [MessageChain] 携带的内部标识
     *
     * 用于 [createMessageReceipt] <- `RemoteFile.uploadAndSend` (文件操作API v1)
     */
    fun MessageChain.dropMiraiInternalFlags(): MessageChain = this
}

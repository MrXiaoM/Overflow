/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.internal

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.network.QQAndroidClient
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal fun Bot.asQQAndroidBot(): QQAndroidBot {
    contract {
        returns() implies (this@asQQAndroidBot is QQAndroidBot)
    }

    return this as QQAndroidBot
}

internal abstract class QQAndroidBot : AbstractBot() {
    abstract val implGetter: () -> cn.evolvefield.onebot.client.core.Bot
    val client: QQAndroidClient by lazy {
        QQAndroidClient(implGetter)
    }
}

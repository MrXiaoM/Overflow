/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.internal.network

import net.mamoe.mirai.internal.network.components.SsoSession

internal open class QQAndroidClient(
    implGetter: () -> cn.evolvefield.onebot.client.core.Bot
) : SsoSession {
    override var loginState: Int = 0
    override var wLoginSigInfo: WLoginSigInfo = WLoginSigInfo(implGetter)
}

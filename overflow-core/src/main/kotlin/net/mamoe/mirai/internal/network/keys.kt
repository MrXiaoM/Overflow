/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("unused")
package net.mamoe.mirai.internal.network

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.QQAndroidBot

/**
 * 兼容 cssxsh/meme-helper 中请求网络接口所需的 bkn、sKey、psKey 等参数
 *
 * https://github.com/cssxsh/meme-helper/blob/61fbfd967a5e45dd9923c470622cd90d5bce1ce4/src/main/kotlin/face/MarketFaceHelper.kt
 */
internal data class WLoginSigInfo(
    val impl: () -> cn.evolvefield.onebot.client.core.Bot
) {
    val bkn: Int
        get() = sKey.encodeToByteArray()
            .fold(5381) { acc: Int, b: Byte -> acc + acc.shl(5) + b.toInt() }
            .and(Int.MAX_VALUE)

    val sKey: String
        get() = runBlocking {
            val data = impl().getCredentials("qun.qq.com").data ?: throw IllegalStateException("Onebot 获取 Credentials (sKey) 失败")
            val matches = Regex("[^_]skey=([^;]+);?").find(data.cookies) ?: throw IllegalStateException("Onebot 获取 Credentials 返回的 cookie 中没有 skey")
            return@runBlocking matches.groupValues[1]
        }

    fun getPsKey(name: String): String {
        return runBlocking {
            val data = impl().getCredentials(name).data ?: throw IllegalStateException("Onebot 获取 Credentials (psKey) 失败")
            val matches = Regex("p_skey=([^;]+);?").find(data.cookies) ?: throw IllegalStateException("Onebot 获取 Credentials 返回的 cookie 中没有 p_skey")
            return@runBlocking matches.groupValues[1]
        }
    }
}

internal val AbstractBot.sKey get() = client.wLoginSigInfo.sKey
internal fun AbstractBot.psKey(name: String) = client.wLoginSigInfo.getPsKey(name)
internal val AbstractBot.client get() = (this as QQAndroidBot).client

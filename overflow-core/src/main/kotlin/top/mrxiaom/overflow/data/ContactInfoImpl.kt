@file:OptIn(LowLevelApi::class)
package top.mrxiaom.overflow.data

import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evole.onebot.sdk.response.contact.StrangerInfoResp
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.StrangerInfo

class FriendInfoImpl(
    override val uin: Long,
    override val nick: String,
    override var remark: String,
    override val friendGroupId: Int = 0
) : FriendInfo

class StrangerInfoImpl(
    override val uin: Long,
    override val nick: String,
    override val fromGroup: Long = 0,
    override val remark: String = "",
): StrangerInfo

val FriendInfo.asOnebot: FriendInfoResp
    get() = FriendInfoResp(uin, nick, remark)
val StrangerInfo.asOnebot: StrangerInfoResp
    get() = StrangerInfoResp(uin, nick, "", 0, "", 0, 0)

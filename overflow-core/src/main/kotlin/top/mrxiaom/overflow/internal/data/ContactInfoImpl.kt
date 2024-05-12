@file:OptIn(LowLevelApi::class)
package top.mrxiaom.overflow.internal.data

import cn.evolvefield.onebot.sdk.response.contact.FriendInfoResp
import cn.evolvefield.onebot.sdk.response.contact.StrangerInfoResp
import cn.evolvefield.onebot.sdk.response.group.GroupMemberInfoResp
import cn.evolvefield.onebot.sdk.util.ignorable
import com.google.gson.JsonObject
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.StrangerInfo

internal class FriendInfoImpl(
    override val uin: Long,
    override val nick: String,
    override var remark: String,
    override val friendGroupId: Int = 0
) : FriendInfo

internal class StrangerInfoImpl(
    override val uin: Long,
    override val nick: String,
    override val fromGroup: Long = 0,
    override val remark: String = "",
): StrangerInfo

internal class MemberInfoImpl(
    override val honors: Set<GroupHonorType>,
    override val isOfficialBot: Boolean,
    override val joinTimestamp: Int,
    override val lastSpeakTimestamp: Int,
    override val muteTimestamp: Int,
    override val nameCard: String,
    override val permission: MemberPermission,
    override val point: Int,
    override val rank: Int,
    override val specialTitle: String,
    override val temperature: Int,
    override val nick: String,
    override val remark: String,
    override val uin: Long
): MemberInfo

internal val FriendInfo.asOnebot: FriendInfoResp
    get() = FriendInfoResp().apply {
        userId = uin
        nickname = nick
        remark = this@asOnebot.remark
    }
internal val StrangerInfo.asOnebot: StrangerInfoResp
    get() = StrangerInfoResp().apply {
        userId = uin
        nickname = nick
        ext = JsonObject().also {
            if (fromGroup > 0) it.addProperty("add_src_id", fromGroup)
        }
    }
internal val StrangerInfoResp.asMirai: StrangerInfo
    get() = StrangerInfoImpl(
        uin = userId,
        nick = nickname,
        fromGroup = ext.ignorable("add_src_id", 0L),
    )

internal val GroupMemberInfoResp.asMirai: MemberInfoImpl
    get() = MemberInfoImpl(setOf(), false, joinTime, lastSentTime, 0, card,
        when(role) {
            "owner" -> MemberPermission.OWNER
            "admin" -> MemberPermission.ADMINISTRATOR
            else -> MemberPermission.MEMBER
        }, 0, 0, title, 0, nickname, "", userId)

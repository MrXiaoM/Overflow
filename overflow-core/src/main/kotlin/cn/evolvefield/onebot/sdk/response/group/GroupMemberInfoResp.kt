package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName

class GroupMemberInfoResp {
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName("nickname")
    var nickname = ""
    @SerializedName("card")
    var card = ""
    @SerializedName("sex")
    var sex = ""
    @SerializedName("age")
    var age = 0
    @SerializedName("area")
    var area = ""
    @SerializedName("join_time")
    var joinTime = 0
    @SerializedName("last_sent_time")
    var lastSentTime = 0
    @SerializedName("level")
    var level = 0
    @SerializedName("qq_level")
    var qqLevel = 0
    @SerializedName("role")
    var role = "member"
    @SerializedName("unfriendly")
    var unfriendly = false
    @SerializedName("title")
    var title = ""
    @SerializedName("title_expire_time")
    var titleExpireTime = 0L
    @SerializedName("card_changeable")
    var cardChangeable = true
    @SerializedName("shut_up_timestamp")
    var shutUpTimestamp = 0L
}

package cn.evolvefield.onebot.client.util

import com.google.gson.JsonObject

class ActionFailedException(
    val app: String,
    val msg: String,
    val json: JsonObject
): OnebotException("请求失败: app=$app, message=$msg,\n    retJson=$json")

open class OnebotException(
    val info: String
): IllegalStateException(info)

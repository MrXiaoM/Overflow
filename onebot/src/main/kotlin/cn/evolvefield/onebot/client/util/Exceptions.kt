package cn.evolvefield.onebot.client.util

import com.google.gson.JsonObject

class ActionFailedException(
    val app: String,
    val msg: String,
    val json: JsonObject
): OnebotException("app=$app, message=$msg, retJson=$json")

open class OnebotException(
    val info: String
): IllegalStateException(info)

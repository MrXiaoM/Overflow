package cn.evolvefield.onebot.client.util

import com.google.gson.JsonObject

class ActionFailedException(
    val app: String,
    val msg: String,
    val json: JsonObject
): IllegalStateException("app=$app, message=$msg, retJson=$json")

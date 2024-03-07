package cn.evolvefield.onebot.client.util

import cn.evole.onebot.sdk.util.json.JsonsObject

class ActionFailedException(
    val app: String,
    val msg: String,
    val json: JsonsObject
): IllegalStateException("app=$app ,message=$msg, retJson=$json")

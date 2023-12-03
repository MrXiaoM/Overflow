package cn.evolvefield.onebot.client.util

import cn.evole.onebot.sdk.util.json.JsonsObject

class ActionFailedException(
    override val message: String = "",
    val json: JsonsObject
): IllegalStateException(message)

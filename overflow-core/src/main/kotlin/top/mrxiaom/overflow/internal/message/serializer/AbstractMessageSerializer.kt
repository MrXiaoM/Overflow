package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import top.mrxiaom.overflow.spi.message.JsonMessageSerializer

abstract class AbstractMessageSerializer : JsonMessageSerializer {
    internal fun JsonObject.data(): JsonObject? {
        return this["data"]?.jsonObject
    }
}

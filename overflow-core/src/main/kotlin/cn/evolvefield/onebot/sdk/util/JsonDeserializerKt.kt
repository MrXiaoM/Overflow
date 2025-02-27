package cn.evolvefield.onebot.sdk.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

interface JsonDeserializerKt<T> : JsonDeserializer<T> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T? {
        return runCatching {
            deserializeFromJson(json, typeOfT, context)
        }.getOrElse {
            throw JsonParseException("An error has occurred while parsing json $json to ${typeOfT.typeName}", it)
        }
    }

    fun deserializeFromJson(json: JsonElement, type: Type, ctx: JsonDeserializationContext): T?
}

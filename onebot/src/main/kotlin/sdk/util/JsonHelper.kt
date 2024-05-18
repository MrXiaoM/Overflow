package cn.evolvefield.onebot.sdk.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.util.function.Supplier

val gson: Gson = GsonBuilder().setPrettyPrinting().create()

fun JsonObject.ignorable(key: String, def: Int): Int {
    return nullableInt(key, def.nullable) ?: def
}
fun JsonObject.nullableInt(key: String, def: Int?): Int? {
    val element = this[key]
    if (element == null || !element.isJsonPrimitive) return def
    return try {
        element.asString.toInt()
    } catch (ignored: NumberFormatException) {
        def
    }
}
fun JsonObject.int(key: String): Int {
    return this[key]?.asInt ?: throw JsonParseException("Can`t find `$key`")
}
fun JsonObject.intFromString(key: String): Int {
    return string(key).toInt()
}
fun JsonObject.ignorable(key: String, def: Long): Long {
    return nullableLong(key, def.nullable) ?: def
}
fun JsonObject.nullableLong(key: String, def: Long?): Long? {
    val element = this[key]
    if (element == null || !element.isJsonPrimitive) return def
    return try {
        element.asString.toLong()
    } catch (ignored: NumberFormatException) {
        def
    }
}
fun JsonObject.longFromString(key: String): Long {
    return string(key).toLong()
}
fun JsonObject.long(key: String): Long {
    return this[key]?.asLong ?: throw JsonParseException("Can`t find `$key`")
}
fun JsonObject.ignorable(key: String, def: Float): Float {
    return nullableFloat(key, def.nullable) ?: def
}
fun JsonObject.nullableFloat(key: String, def: Float?): Float? {
    val element = this[key]
    if (element == null || !element.isJsonPrimitive) return def
    return try {
        element.asString.toFloat()
    } catch (ignored: NumberFormatException) {
        def
    }
}
fun JsonObject.floatFromString(key: String): Float {
    return string(key).toFloat()
}
fun JsonObject.float(key: String): Float {
    return this[key]?.asFloat ?: throw JsonParseException("Can`t find `$key`")
}
fun JsonObject.ignorable(key: String, def: Double): Double {
    return nullableDouble(key, def.nullable) ?: def
}
fun JsonObject.nullableDouble(key: String, def: Double?): Double? {
    val element = this[key]
    if (element == null || !element.isJsonPrimitive) return def
    return try {
        element.asString.toDouble()
    } catch (ignored: NumberFormatException) {
        def
    }
}
fun JsonObject.doubleFromString(key: String): Double {
    return string(key).toDouble()
}
fun JsonObject.double(key: String): Double {
    return this[key]?.asDouble ?: throw JsonParseException("Can`t find `$key`")
}
fun JsonObject.ignorable(key: String, def: String): String {
    return nullableString(key, def.nullable) ?: def
}
fun JsonObject.nullableString(key: String, def: String?): String? {
    val element = this[key]
    if (element == null || !element.isJsonPrimitive) return def
    return element.asString
}
fun JsonObject.string(key: String): String {
    return this[key]?.asString ?: throw JsonParseException("Can`t find `$key`")
}

fun JsonObject.ignorableObject(key: String, def: Supplier<JsonObject>): JsonObject {
    val element = this[key]
    if (element == null || !element.isJsonObject) return def.get()
    return element.asJsonObject
}

fun JsonObject.ignorableArray(key: String, def: Supplier<JsonArray>): JsonArray {
    val element = this[key]
    if (element == null || !element.isJsonArray) return def.get()
    return element.asJsonArray
}

fun JsonObject.forceString(key: String): String {
    val element = this[key] ?: return ""
    if (element.isJsonPrimitive) return element.asString
    return gson.toJson(element)
}

fun <T> JsonObject.fromJson(key: String, type: Class<T>): T? {
    return gson.fromJson(this[key], type)
}
inline fun <reified T : Any> JsonObject.fromJson(key: String): T? {
    return gson.fromJson(this[key], T::class.java)
}
inline fun <reified T> JsonElement.withToken(): T {
    return gson.fromJson(deepCopyIgnoreNulls(), object : TypeToken<T>() {}.type)
}
inline fun <reified T> JsonElement.withClass(): T {
    return gson.fromJson(deepCopyIgnoreNulls(), T::class.java)
}
fun JsonElement?.deepCopyIgnoreNulls(): JsonElement? {
    if (this == null) return null
    return ignoreNulls(this) ?: deepCopy()
}
fun ignoreNulls(json: JsonElement): JsonElement? {
    return when (json) {
        is JsonObject -> {
            val members = json.asMap()
            JsonObject().apply {
                for ((key, value) in members) {
                    ignoreNulls(value)?.also { add(key, it) }
                }
            }
        }
        is JsonArray -> {
            val elements = json.asList()
            if (elements.isNotEmpty()) {
                JsonArray(elements.size).apply {
                    for (element in elements) {
                        ignoreNulls(element)?.also { add(it) }
                    }
                }
            }
            JsonArray()
        }
        is JsonPrimitive -> json.deepCopy()

        else -> null
    }
}
fun <T> List<T>.toJsonArray(): JsonArray {
    val array = JsonArray()
    for (any in this) {
        when (any) {
            is JsonElement -> array.add(any)
            is Map<*, *> -> array.add(any.toJsonObject())
            is List<*> -> array.add(any.toJsonArray())
            is Boolean -> array.add(any)
            is Number -> array.add(any)
            is Char -> array.add(any)
            else -> array.add(any.toString())
        }
    }
    return array
}
fun <K,V> Map<K, V>.toJsonObject(): JsonObject {
    val obj = JsonObject()
    for (entry in entries) {
        val key = entry.key.toString()
        when (val value = entry.value) {
            is JsonElement -> obj.add(key, value)
            is Map<*, *> -> obj.add(key, value.toJsonObject())
            is List<*> -> obj.add(key, value.toJsonArray())
            is Boolean -> obj.addProperty(key, value)
            is Number -> obj.addProperty(key, value)
            is Char -> obj.addProperty(key, value)
            else -> obj.addProperty(key, value.toString())
        }
    }
    return obj
}
val <T> T.nullable: T?
    get() = this

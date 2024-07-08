package cn.evolvefield.onebot.sdk.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

object CQCode {
    fun fromJson(json: JsonArray): String {
        return buildString {
            for (ele in json) {
                val obj = ele.asJsonObject
                val type = obj["type"].asString
                val data = obj["data"]
                if (data is JsonObject) {
                    // 纯文本
                    if (type == "text") {
                        append(data["text"].asString
                            .replace("&", "&amp;")
                            .replace("[", "&#91;")
                            .replace("]", "&#93;"))
                        continue
                    }
                    append("[CQ:$type")
                    for ((key, value) in data.asMap()) {
                        // 约定: key 不会出现需要转义的字符
                        append(",$key=")
                        append(
                            if (value is JsonPrimitive) {
                                value.asString
                            } else {
                                value.toString()
                            }.replace("&", "&amp;")
                                .replace("[", "&#91;")
                                .replace("]", "&#93;")
                                .replace(",", "&#44;")
                        )
                    }
                }
                append("]")
            }
        }
    }

    fun toJson(s: String): JsonArray {
        val array = JsonArray()
        fun add(type: String, data: JsonObject.() -> Unit) {
            array.add(JsonObject().also {
                it.addProperty("type", type)
                it.add("data", JsonObject().apply(data))
            })
        }
        fun String.decode(): String {
            return replace("&amp", "&")
                .replace("&#91;", "[")
                .replace("&#93;", "]")
        }
        fun String.decodeValue(): String {
            return decode()
                .replace("&#44;", ",")
        }
        fun StringBuilder.decode(): String {
            return toString()
                .decode()
                .also { clear() }
        }
        val temp = StringBuilder()
        var flag = false
        for (c in s) {
            if (c == '[' && temp.isNotEmpty()) {
                add("text") {
                    addProperty("text", temp.decode())
                }
            }
            temp.append(c)
            if (!flag && temp.startsWith("[CQ:")) flag = true
            if (flag && c == ']') {
                val cq = temp.substring(4, temp.length - 1)
                if (cq.contains(',')) {
                    val split = cq.split(',')
                    add(split[0]) {
                        for (s1 in split.drop(1)) {
                            if (!s1.contains('=')) continue
                            val (key, value) = s1
                                .split('=', limit = 2)
                                .run { this[0] to this[1].decodeValue() }
                            addProperty(key, value)
                        }
                    }
                } else add(cq) { }
                temp.clear()
                flag = false
            }
        }
        if (temp.isNotEmpty()) {
            add("text") {
                addProperty("text", temp.decode())
            }
        }
        return array
    }
}

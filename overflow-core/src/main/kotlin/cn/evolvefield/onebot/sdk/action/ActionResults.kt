package cn.evolvefield.onebot.sdk.action

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.util.*

open class JsonContainer {
    internal lateinit var element: JsonElement
    val json: JsonElement
        get() = element
}

data class ActionRaw(
    @SerializedName("status")
    val status: String,
    @SerializedName("retCode")
    val retCode: Int,
    @SerializedName("echo")
    val echo: String? = null,
): JsonContainer()

data class ActionData<T : Any> (
    @SerializedName("status")
    val status: String,
    @SerializedName("retcode")
    val retCode: Int,
    @SerializedName("data")
    val data: T?,
    @SerializedName("echo")
    val echo: String? = null
): JsonContainer()

data class ActionList<T : Any>(
    @SerializedName("status")
    val status: String,
    @SerializedName("retcode")
    val retCode: Int,
    @SerializedName("data")
    val data: LinkedList<T>,
    @SerializedName("echo")
    val echo: String? = null
): JsonContainer()

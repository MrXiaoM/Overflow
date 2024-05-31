package cn.evolvefield.onebot.sdk.action

import com.google.gson.annotations.SerializedName
import java.util.LinkedList

data class ActionRaw(
    @SerializedName("status")
    val status: String,
    @SerializedName("retCode")
    val retCode: Int,
    @SerializedName("echo")
    val echo: String? = null
)

data class ActionData<T : Any> (
    @SerializedName("status")
    val status: String,
    @SerializedName("retcode")
    val retCode: Int,
    @SerializedName("data")
    val data: T?,
    @SerializedName("echo")
    val echo: String? = null
)

data class ActionList<T : Any>(
    @SerializedName("status")
    val status: String,
    @SerializedName("retcode")
    val retCode: Int,
    @SerializedName("data")
    val data: LinkedList<T>,
    @SerializedName("echo")
    val echo: String? = null
)

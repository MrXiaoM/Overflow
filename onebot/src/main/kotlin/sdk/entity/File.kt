package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName

/**
 * 文件实体
 */
class File {
    @SerializedName("id")
    var id = ""
    @SerializedName("name")
    var name = ""
    @SerializedName("size")
    var size = 0L
    @SerializedName("busid")
    var busId = 0L
    @SerializedName("url")
    var url = ""
}

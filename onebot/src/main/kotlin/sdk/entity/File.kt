package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName
import lombok.Data

/**
 * 文件实体
 */
@Data
class File {
    @SerializedName("id")
    var id = ""
    @SerializedName("name")
    var name = ""
    @SerializedName("size")
    var size = 0L
    @SerializedName("busid")
    var busId = 0L
}

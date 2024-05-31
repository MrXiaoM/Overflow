package cn.evolvefield.onebot.sdk.response.ext

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@AllArgsConstructor
@NoArgsConstructor
class GetFileResp  {
    @SerializedName("file")
    var file: String = ""
    @SerializedName("file_name")
    var fileName: String = ""
    @SerializedName("file_size")
    var fileSize: Long = 0
    @SerializedName("base64")
    var base64: String? = null
}

package cn.evolvefield.onebot.sdk.response.group

import cn.evolvefield.onebot.sdk.util.json.GroupFilesAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GroupFilesResp {

    var files: List<Files>? = null

    var folders: List<Folders>? = null

    /**
     * 群文件
     */
    @Data
    @JsonAdapter(GroupFilesAdapter.Files::class)
    class Files {
        var fileId = ""
        var fileName = ""
        var busid = 0
        var fileSize = 0L
        var uploadTime = 0L
        var deadTime = 0L
        var modifyTime = 0L
        var downloadTimes = 0
        var uploader = 0L
        var uploaderName = ""
        var md5: String? = null
        var sha1: String? = null
        var sha3: String? = null
    }

    /**
     * 群文件夹
     */
    @Data
    class Folders {
        @SerializedName("folder_id")
        var folderId = ""
        @SerializedName("folder_name")
        var folderName = ""
        @SerializedName("create_time")
        var createTime = 0L
        @SerializedName("creator")
        var creator = 0L
        @SerializedName("creator_name")
        var creatorName = ""
        @SerializedName("total_file_count")
        var totalFileCount = 0
    }
}

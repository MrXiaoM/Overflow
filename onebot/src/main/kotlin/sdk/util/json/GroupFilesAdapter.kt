package cn.evolvefield.onebot.sdk.util.json

import cn.evolvefield.onebot.sdk.response.group.GroupFilesResp
import cn.evolvefield.onebot.sdk.util.*
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class GroupFilesAdapter {
    class Files : JsonDeserializer<GroupFilesResp.Files> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): GroupFilesResp.Files {
            val obj = json.asJsonObject
            return GroupFilesResp.Files().apply {
                fileId = obj.string("file_id")
                fileName = obj.string("file_name")
                busid = obj.int("busid")
                fileSize = obj.long("file_size")
                uploadTime = obj.long("upload_time")
                deadTime = obj.long("dead_time")
                modifyTime = obj.long("modify_time")
                downloadTimes = obj.int("download_times")
                uploader = obj.long("uploader")
                uploaderName = obj.string("uploader_name")
                md5 = obj.nullableString("md5", null)
                sha1 = obj.nullableString("sha1", null)
                sha3 = obj.nullableString("sha3", null)
            }
        }
    }
}

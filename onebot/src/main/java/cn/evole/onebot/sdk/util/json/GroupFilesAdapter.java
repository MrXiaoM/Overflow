package cn.evole.onebot.sdk.util.json;


import cn.evole.onebot.sdk.response.group.GroupFilesResp;
import com.google.gson.*;

import java.lang.reflect.Type;

public class GroupFilesAdapter {
    Gson gson = new Gson();
    public static class Files implements JsonDeserializer<GroupFilesResp.Files> {

        public GroupFilesResp.Files deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject jsonObj = json.getAsJsonObject();
            GroupFilesResp.Files files = new GroupFilesResp.Files();
            files.fileId = jsonObj.get("file_id").getAsString();
            files.fileName = jsonObj.get("file_name").getAsString();
            files.busid = jsonObj.get("busid").getAsInt();
            files.fileSize = jsonObj.get("file_size").getAsLong();
            files.uploadTime = jsonObj.get("upload_time").getAsLong();
            files.deadTime = jsonObj.get("dead_time").getAsLong();
            files.modifyTime = jsonObj.get("modify_time").getAsLong();
            files.downloadTimes = jsonObj.get("download_times").getAsInt();
            files.uploader = jsonObj.get("uploader").getAsLong();
            files.uploaderName = jsonObj.get("uploader_name").getAsString();
            JsonElement jsonMd5 = jsonObj.get("md5");
            if (jsonMd5 != null) {
                files.md5 = jsonMd5.getAsString();
            }
            JsonElement jsonSha1 = jsonObj.get("sha1");
            if (jsonSha1 != null) {
                files.sha1 = jsonSha1.getAsString();
            }
            JsonElement jsonSha3 = jsonObj.get("sha3");
            if (jsonSha3 != null) {
                files.sha3 = jsonSha3.getAsString();
            }
            return files;
        }
    }
}
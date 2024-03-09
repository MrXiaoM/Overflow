package cn.evole.onebot.sdk.util.json;

import cn.evole.onebot.sdk.response.group.GroupFilesResp;
import cn.evole.onebot.sdk.util.JsonHelper;
import com.google.gson.*;

import java.lang.reflect.Type;

public class GroupFilesAdapter {
    public static class Files extends JsonHelper implements JsonDeserializer<GroupFilesResp.Files> {
        public GroupFilesResp.Files deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            GroupFilesResp.Files files = new GroupFilesResp.Files();
            files.fileId = obj.get("file_id").getAsString();
            files.fileName = obj.get("file_name").getAsString();
            files.busid = obj.get("busid").getAsInt();
            files.fileSize = obj.get("file_size").getAsLong();
            files.uploadTime = obj.get("upload_time").getAsLong();
            files.deadTime = obj.get("dead_time").getAsLong();
            files.modifyTime = obj.get("modify_time").getAsLong();
            files.downloadTimes = obj.get("download_times").getAsInt();
            files.uploader = obj.get("uploader").getAsLong();
            files.uploaderName = obj.get("uploader_name").getAsString();
            files.md5 = ignorable(obj, "md5", null);
            files.sha1 = ignorable(obj, "sha1", null);
            files.sha3 = ignorable(obj, "sha3", null);
            return files;
        }
    }
}
package cn.evole.onebot.sdk.response.group;

import cn.evole.onebot.sdk.util.json.GroupFilesAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
public class GroupFilesResp {

    public List<Files> files;

    public List<Folders> folders;

    /**
     * 群文件
     */
    @Data
    @JsonAdapter(GroupFilesAdapter.Files.class)
    public static class Files {
        public String fileId;
        public String fileName;
        public int busid;
        public long fileSize;
        public long uploadTime;
        public long deadTime;
        public long modifyTime;
        public int downloadTimes;
        public long uploader;
        public String uploaderName;
        public String md5 = null;
        public String sha1 = null;
        public String sha3 = null;
    }

    /**
     * 群文件夹
     */
    @Data
    public static class Folders {

        @SerializedName("folder_id")
        public String folderId;

        @SerializedName("folder_name")
        public String folderName;

        @SerializedName("create_time")
        public long createTime;

        public long creator;

        @SerializedName("creator_name")
        public String creatorName;

        @SerializedName("total_file_count")
        public int totalFileCount;

    }

}

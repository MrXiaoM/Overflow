package cn.evole.onebot.sdk.response.group;

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
    public static class Files {

        @SerializedName("file_id")
        public String fileId;

        @SerializedName("file_name")
        public String fileName;

        public int busid;

        @SerializedName("file_size")
        public long fileSize;

        @SerializedName("upload_time")
        public long uploadTime;

        @SerializedName("dead_time")
        public long deadTime;

        @SerializedName("modify_time")
        public long modifyTime;

        @SerializedName("download_times")
        public int downloadTimes;

        public long uploader;

        @SerializedName("uploader_name")
        public String uploaderName;

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

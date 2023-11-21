package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
public class GroupFilesResp {

    private List<Files> files;

    private List<Folders> folders;

    /**
     * 群文件
     */
    @Data
    public static class Files {

        @SerializedName("file_id")
        private String fileId;

        @SerializedName("file_name")
        private String fileName;

        private int busid;

        @SerializedName("file_size")
        private long fileSize;

        @SerializedName("upload_time")
        private long uploadTime;

        @SerializedName("dead_time")
        private long deadTime;

        @SerializedName("modify_time")
        private long modifyTime;

        @SerializedName("download_times")
        private int downloadTimes;

        private long uploader;

        @SerializedName("uploader_name")
        private String uploaderName;

    }

    /**
     * 群文件夹
     */
    @Data
    public static class Folders {

        @SerializedName("folder_id")
        private String folderId;

        @SerializedName("folder_name")
        private String folderName;

        @SerializedName("create_time")
        private long createTime;

        private long creator;

        @SerializedName("creator_name")
        private String creatorName;

        @SerializedName("total_file_count")
        private int totalFileCount;

    }

}

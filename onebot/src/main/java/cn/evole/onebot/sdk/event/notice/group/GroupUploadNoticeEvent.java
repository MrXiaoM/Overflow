package cn.evole.onebot.sdk.event.notice.group;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class GroupUploadNoticeEvent extends NoticeEvent {

    @SerializedName( "group_id")
    private long groupId;

    @SerializedName( "file")
    private File file;

    /**
     * 文件实体
     */
    @Data
    public static class File {

        @SerializedName( "id")
        private String id;

        @SerializedName( "name")
        private String name;

        @SerializedName( "size")
        private long size;

        @SerializedName( "busid")
        private long busid;

    }

}

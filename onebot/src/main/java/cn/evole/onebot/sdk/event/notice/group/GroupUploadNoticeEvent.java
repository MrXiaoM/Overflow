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
    public long groupId;

    @SerializedName( "file")
    private File file;

    /**
     * 文件实体
     */
    @Data
    public static class File {

        @SerializedName( "id")
        public String id;

        @SerializedName( "name")
        public String name;

        @SerializedName( "size")
        public long size;

        @SerializedName( "busid")
        public long busid;

    }

}

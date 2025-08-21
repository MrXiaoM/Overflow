package cn.evolvefield.onebot.sdk.response.ext

import com.google.gson.annotations.SerializedName

/**
 * LLOnebot, NapCat
 */
class CreateGroupFileFolderResp {
    @SerializedName("folder_id")
    var folderId = ""

    @SerializedName("groupItem")
    var groupItem: GroupItem? = null

    class GroupItem{
        @SerializedName("folderInfo")
        var folderInfo: FolderInfo? = null

        class FolderInfo{
            @SerializedName("folderId")
            var folderId = ""
        }
    }
}

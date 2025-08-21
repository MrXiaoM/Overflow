package top.mrxiaom.overflow.internal.contact.data

import cn.evolvefield.onebot.sdk.response.group.GroupFilesResp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.*
import top.mrxiaom.overflow.internal.contact.GroupWrapper
import top.mrxiaom.overflow.internal.contact.data.RemoteFilesWrapper.Companion.update
import top.mrxiaom.overflow.internal.message.data.WrappedFileMessage
import top.mrxiaom.overflow.internal.utils.toMiraiFile
import top.mrxiaom.overflow.internal.utils.toMiraiFiles
import top.mrxiaom.overflow.internal.utils.toMiraiFolders
import top.mrxiaom.overflow.spi.FileService
import java.net.URLEncoder
import java.util.stream.Stream

internal class RemoteFilesWrapper(
    override val contact: GroupWrapper,
    override val root: FolderWrapper
) : RemoteFiles {
    companion object {
        internal suspend fun GroupWrapper.fetchFiles(): RemoteFilesWrapper {
            val data = bot.impl.getGroupRootFiles(id) {
                throwExceptions(null)
            }.data

            val root = FolderWrapper(
                this, null, "/", "/", 0, 0, 0, data?.files?.size ?: 0
            )
            if (data != null) {
                root.update(data)
            } else {
                bot.logger.warning("获取群 $id 的文件列表失败，可能是 Onebot 实现不支持，详见网络日志")
            }
            return RemoteFilesWrapper(this, root)
        }

        internal fun FolderWrapper.update(data: GroupFilesResp) {
            data.folders?.toMiraiFolders(contact, this)?.also { folders.addAll(it) }
            data.files?.toMiraiFiles(contact, this)?.also { files.addAll(it) }
        }
    }
}

internal class FolderWrapper(
    override val contact: GroupWrapper,
    override val parent: AbsoluteFolder? = null,
    override val id: String,
    override val name: String,
    override val lastModifiedTime: Long,
    override val uploadTime: Long,
    override val uploaderId: Long,
    override var contentsCount: Int,
) : AbsoluteFolder {
    internal val impl = contact.bot.impl
    internal val folders: MutableList<FolderWrapper> = mutableListOf()
    internal val files: MutableList<FileWrapper> = mutableListOf()

    override val isFolder: Boolean = true
    override val isFile: Boolean = false

    override val absolutePath: String
        get() {
            val parent = parent
            return when {
                parent == null || this.id == "/" -> "/"
                parent.parent == null || parent.id == "/" -> "/$name"
                else -> "${parent.absolutePath}/$name"
            }
        }

    override suspend fun delete(): Boolean {
        return impl.deleteGroupFolder(contact.id, id).status != "failed"
    }

    override suspend fun exists(): Boolean {
        return true // TODO: 获取该文件夹当前是否还存在
    }

    override suspend fun files(): Flow<AbsoluteFile> {
        return files.toList().asFlow()
    }

    override suspend fun refresh(): Boolean {
        return refreshed() != null
    }

    override suspend fun renameTo(newName: String): Boolean {
        TODO("暂无重命名文件夹实现")
    }

    override suspend fun children(): Flow<AbsoluteFileFolder> {
        return folders.plus(files).asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun childrenStream(): Stream<AbsoluteFileFolder> {
        return folders.plus(files).stream()
    }

    override suspend fun createFolder(name: String): AbsoluteFolder {
        if (name.isEmpty()) throw IllegalArgumentException("子目录名称不能为空")
        if (name.matches(Regex(":*?\"<>|"))) throw IllegalArgumentException("不能在非根目录下创建子目录")

        // 与 mirai 行为保持一致
        this.resolveFolder(name)?.let {
            return it
        }
        val result = contact.bot.impl.createGroupFileFolder(contact.id, name, id)
        val data = result.data ?: throw PermissionDeniedException("当前 Onebot 实现不支持获取文件夹创建回执")
        val folderId = if (data.folderId.isNotEmpty()) {
            data.folderId
        } else if (data.groupItem?.folderInfo?.folderId?.isNotEmpty() ?: false) {
            data.groupItem?.folderInfo?.folderId!!
        } else throw PermissionDeniedException("当前 Onebot 实现不支持获取文件夹创建回执")
        val time = currentTimeSeconds()
        val folder = FolderWrapper(contact, this, folderId, name, time, time, contact.bot.id, 0)
        folders.add(folder)
        return folder
    }

    @JavaFriendlyAPI
    override suspend fun filesStream(): Stream<AbsoluteFile> {
        return files.toList<AbsoluteFile>().stream()
    }

    override suspend fun folders(): Flow<AbsoluteFolder> {
        return folders.asFlow()
    }

    @JavaFriendlyAPI
    override suspend fun foldersStream(): Stream<AbsoluteFolder> {
        return folders.toList<AbsoluteFolder>().stream()
    }

    suspend fun refreshFromOnebot(): GroupFilesResp? {
        return contact.bot.impl.run {
            val id = this@FolderWrapper.id
            if (id == "/") {
                getGroupRootFiles(contact.id) {
                    throwExceptions(null)
                }.data
            } else {
                getGroupFilesByFolder(contact.id, id) {
                    throwExceptions(null)
                }.data
            }
        }
    }

    override suspend fun refreshed(): AbsoluteFolder? {
        val data = refreshFromOnebot()
        update(data ?: return null)
        return this
    }

    override suspend fun resolveAll(path: String): Flow<AbsoluteFileFolder> {
        return children().filter { it.name == path }
    }

    @JavaFriendlyAPI
    override suspend fun resolveAllStream(path: String): Stream<AbsoluteFileFolder> {
        return childrenStream().filter { it.name == path }
    }

    override suspend fun resolveFileById(id: String, deep: Boolean): AbsoluteFile? {
        if (deep) {
            TODO("暂不支持深入子目录查找文件")
        }
        return files.firstOrNull { it.id == id }
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        // TODO: 获取子目录内的文件
        return files().filter { it.absolutePath.removePrefix(absolutePath) == path }
    }

    @JavaFriendlyAPI
    override suspend fun resolveFilesStream(path: String): Stream<AbsoluteFile> {
        // TODO: 获取子目录内的文件
        return filesStream().filter { it.absolutePath.removePrefix(absolutePath) == path }
    }

    override suspend fun resolveFolder(name: String): AbsoluteFolder? {
        if (name.isEmpty()) throw IllegalArgumentException("子目录名称不能为空")
        if (name.matches(Regex(":*?\"<>|"))) throw IllegalArgumentException("请求子目录包含不允许的字符")
        return folders().firstOrNull { it.name == name }
    }

    override suspend fun resolveFolderById(id: String): AbsoluteFolder? {
        if (name.isEmpty()) throw IllegalArgumentException("子目录名称不能为空")
        return folders().firstOrNull { it.id == id }
    }

    @Suppress("INVISIBLE_MEMBER")
    override suspend fun uploadNewFile(
        filepath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile {
        val result = impl.uploadGroupFile(contact.id, FileService.instance!!.upload(content), filepath, id)
        if (result.status == "ok") {
            val data = result.data
            val id = data?.fileId?.takeIf { it.isNotEmpty() }
            val name = filepath.substringAfterLast('/')
            val size = content.size
            val uploadTime = currentTimeSeconds()
            val md5 = content.md5
            val sha1 = content.sha1
            if (id != null) {
                val file = FileWrapper(contact, this, id, name, md5, sha1, size, 0L, uploadTime, uploadTime, contact.bot.id, null)
                files.add(file)
                return file
            }
            return DummyFile(contact, this, name, md5, sha1, size, uploadTime, contact.bot.id)
        }
        throw PermissionDeniedException("文件上传失败，详见网络日志 (logs/onebot)")
    }

    override fun toString(): String = "AbsoluteFolder(name=$name, absolutePath=$absolutePath, id=$id)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false
        if (!super.equals(other)) return false

        if (contentsCount != other.contentsCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + contentsCount.hashCode()
        return result
    }
}

internal class FileWrapper(
    override val contact: GroupWrapper,
    override var parent: FolderWrapper,
    override val id: String,
    override val name: String,
    override val md5: ByteArray,
    override val sha1: ByteArray,
    override val size: Long,
    override val expiryTime: Long,
    override val lastModifiedTime: Long,
    override val uploadTime: Long,
    override val uploaderId: Long,
    val busid: Int?,
) : AbsoluteFile {
    internal val impl = contact.bot.impl
    override val isFile: Boolean = true
    override val isFolder: Boolean = false

    override val absolutePath: String
        get() {
            val parent = parent
            return when {
                parent.name == "/" -> "/$name"
                else -> "${parent.absolutePath}/$name"
            }
        }

    override suspend fun delete(): Boolean {
        return impl.deleteGroupFile(contact.id, id, busid).status != "failed"
    }

    override suspend fun exists(): Boolean {
        return getUrl() != null
    }

    override suspend fun getUrl(): String? {
        when (contact.bot.appName.lowercase()) {
            "llonebot", "napcat" -> {
                val file = contact.bot.impl.extGetFile(id).data
                return file?.file?.run { "file:///${URLEncoder.encode(this, "UTF-8")}" }
            }
            else -> return impl.getGroupFileUrl(contact.id, id, busid).data?.url
        }
    }

    override suspend fun moveTo(folder: AbsoluteFolder): Boolean {
        if (folder.absolutePath == this.parent.absolutePath) return true
        if (!contact.bot.appName.lowercase().contains("napcat")) {
            throw PermissionDeniedException("当前 Onebot 实现不支持移动文件")
        }
        if (folder !is FolderWrapper)
            return false
        val success =
            impl.moveGroupFIle(contact.id, id, parent.id, folder.id).data?.ok ?: false
        if (success) {
            parent.files.remove(this)
            parent = folder
            folder.files.add(this)
        }
        return success
    }

    override suspend fun refresh(): Boolean {
        return refreshed() != null
    }

    override suspend fun refreshed(): AbsoluteFile? {
        val data = parent.refreshFromOnebot() ?: return null
        return data.files?.firstOrNull { it.fileId == id }?.toMiraiFile(contact, parent)
    }

    override suspend fun renameTo(newName: String): Boolean {
        TODO("暂无重命名文件实现")
    }

    override fun toMessage(): FileMessage {
        return WrappedFileMessage(id, 0, name, size)
    }

    override fun toString(): String = "AbsoluteFile(name=$name, absolutePath=$absolutePath, id=$id)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileWrapper || !isSameClass(this, other)) return false
        if (!super.equals(other)) return false

        if (expiryTime != other.expiryTime) return false
        if (size != other.size) return false
        if (!sha1.contentEquals(other.sha1)) return false
        if (!md5.contentEquals(other.md5)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + expiryTime.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + sha1.contentHashCode()
        result = 31 * result + md5.contentHashCode()
        return result
    }
}

/**
 * 在实现获取文件发送回执之前的临时实现
 */
internal class DummyFile(
    override val contact: FileSupported,
    override val parent: AbsoluteFolder?,
    override val name: String,
    override val md5: ByteArray,
    override val sha1: ByteArray,
    override val size: Long,
    override val uploadTime: Long,
    override val uploaderId: Long
): AbsoluteFile {
    override val expiryTime: Long = 0L
    override val id: String
        get() = throw IllegalStateException("很不幸，Onebot 没有提供获取文件上传回执的方法，刚上传的文件获取到的回执无法使用")
    override val isFile: Boolean = true
    override val isFolder: Boolean = false
    override val lastModifiedTime: Long = uploadTime
    override val absolutePath: String
        get() {
            val parent = parent
            return when {
                parent == null || this.id == "/" -> "/"
                parent.parent == null || parent.id == "/" -> "/$name"
                else -> "${parent.absolutePath}/$name"
            }
        }

    override suspend fun delete(): Boolean = false
    override suspend fun exists(): Boolean = true
    override suspend fun getUrl(): String? {
        throw IllegalStateException("很不幸，Onebot 没有提供获取文件上传回执的方法，刚上传的文件获取到的回执无法使用")
    }
    override suspend fun moveTo(folder: AbsoluteFolder): Boolean = false
    override suspend fun refresh(): Boolean = false
    override suspend fun refreshed(): AbsoluteFile? = null
    override suspend fun renameTo(newName: String): Boolean = false
    override fun toMessage(): FileMessage {
        throw IllegalStateException("很不幸，Onebot 没有提供获取文件上传回执的方法，刚上传的文件获取到的回执无法使用")
    }
    override fun toString(): String = "DummyFile(name=$name, absolutePath=$absolutePath, id=DUMMY)"
}

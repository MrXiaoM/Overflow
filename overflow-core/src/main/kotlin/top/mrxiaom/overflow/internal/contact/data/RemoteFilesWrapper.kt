package top.mrxiaom.overflow.internal.contact.data

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.*
import top.mrxiaom.overflow.internal.contact.GroupWrapper
import top.mrxiaom.overflow.internal.message.data.WrappedFileMessage
import top.mrxiaom.overflow.internal.utils.toMiraiFiles
import top.mrxiaom.overflow.internal.utils.toMiraiFolders
import top.mrxiaom.overflow.spi.FileService
import java.util.stream.Stream

internal class RemoteFilesWrapper(
    override val contact: GroupWrapper,
    override val root: FolderWrapper
) : RemoteFiles {
    companion object {
        internal suspend fun GroupWrapper.fetchFiles(): RemoteFilesWrapper {
            val data = botWrapper.impl.getGroupRootFiles(id).data

            val root = FolderWrapper(
                this, null, "/", "/", 0, 0, 0, data.files.size
            )
            root.folders.addAll(data.folders.toMiraiFolders(this))
            root.files.addAll(data.files.toMiraiFiles(this))
            return RemoteFilesWrapper(this, root)
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
    internal val impl = contact.botWrapper.impl
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
        TODO("Not yet implemented")
    }

    override suspend fun files(): Flow<AbsoluteFile> {
        TODO("Not yet implemented")
    }

    override suspend fun refresh(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun renameTo(newName: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun children(): Flow<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun childrenStream(): Stream<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun createFolder(name: String): AbsoluteFolder {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun filesStream(): Stream<AbsoluteFile> {
        TODO("Not yet implemented")
    }

    override suspend fun folders(): Flow<AbsoluteFolder> {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun foldersStream(): Stream<AbsoluteFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshed(): AbsoluteFolder? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveAll(path: String): Flow<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun resolveAllStream(path: String): Stream<AbsoluteFileFolder> {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFileById(id: String, deep: Boolean): AbsoluteFile? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFiles(path: String): Flow<AbsoluteFile> {
        TODO("Not yet implemented")
    }

    @JavaFriendlyAPI
    override suspend fun resolveFilesStream(path: String): Stream<AbsoluteFile> {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFolder(name: String): AbsoluteFolder? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFolderById(id: String): AbsoluteFolder? {
        TODO("Not yet implemented")
    }

    @Suppress("INVISIBLE_MEMBER")
    override suspend fun uploadNewFile(
        filepath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile {
        impl.uploadGroupFile(contact.id, FileService.instance!!.upload(content), filepath, id)
        TODO("文件上传回执")
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
    override val parent: FolderWrapper?,
    override val id: String,
    override val name: String,
    override val md5: ByteArray,
    override val sha1: ByteArray,
    override val size: Long,
    override val expiryTime: Long,
    override val lastModifiedTime: Long,
    override val uploadTime: Long,
    override val uploaderId: Long,
    val busid: Int,
) : AbsoluteFile {
    internal val impl = contact.botWrapper.impl
    override val isFile: Boolean = true
    override val isFolder: Boolean = false

    override val absolutePath: String
        get() {
            val parent = parent
            return when {
                parent == null || parent.name == "/" -> "/$name"
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
        return impl.getGroupFileUrl(contact.id, id, busid).data?.url
    }

    override suspend fun moveTo(folder: AbsoluteFolder): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refresh(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refreshed(): AbsoluteFile? {
        TODO("Not yet implemented")
    }

    override suspend fun renameTo(newName: String): Boolean {
        TODO("Not yet implemented")
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

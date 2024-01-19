package top.mrxiaom.overflow.internal.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.message.data.FileMessage

@Serializable
internal data class WrappedFileMessage(
    override val id: String,
    override val internalId: Int,
    override val name: String,
    override val size: Long,
    val url: String = ""
) : FileMessage {
    override suspend fun toAbsoluteFile(contact: FileSupported): AbsoluteFile? {
        return contact.files.root.resolveFileById(id, true)
    }
}
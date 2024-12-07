package top.mrxiaom.overflow.message.data

import net.mamoe.mirai.message.data.FileMessage

/**
 * 带链接的文件消息
 *
 * 注意，这个消息类型提供的文件链接并不可靠，可能为空字符串。
 *
 * 基本上，仅在开启 use_group_upload_event_for_file_message 的情况下，从消息收到文件消息时可提供链接。
 */
interface FileMessageWithUrl : FileMessage {
    val url: String
}

val FileMessage.withUrl: FileMessageWithUrl
    get() = this as FileMessageWithUrl

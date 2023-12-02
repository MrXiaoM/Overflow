package top.mrxiaom.overflow.internal.utils

import net.mamoe.mirai.utils.ExternalResource
import top.mrxiaom.overflow.spi.FileService

/**
 * 文件服务默认实现
 */
class Base64FileService : FileService {
    override val priority: Int = 1000
    override suspend fun upload(res: ExternalResource): String = res.toBase64File()
}

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.spi

import net.mamoe.mirai.spi.BaseService
import net.mamoe.mirai.spi.SpiServiceLoader
import net.mamoe.mirai.utils.ExternalResource

/**
 * 文件服务，用于图片、语音、端视频文件上传
 */
interface FileService : BaseService {
    /**
     * 上传文件并返回链接
     *
     * @return Onebot 格式链接，以 `file:///`、`http(s)://` 或 `base64://` 开头
     */
    suspend fun upload(res: ExternalResource): String

    companion object {
        private val loader = SpiServiceLoader(FileService::class)

        internal val instance: FileService?
            get() = loader.service
    }
}

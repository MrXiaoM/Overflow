package top.mrxiaom.overflow.utils

import net.mamoe.mirai.utils.ExternalResource
import java.util.*

object ResourceUtils {
    fun ExternalResource.toBase64File(): String {
        return inputStream().use {
            "base64://" + Base64.getEncoder().encodeToString(it.readBytes())
        }
    }
}
package top.mrxiaom.overflow.internal.utils

import net.mamoe.mirai.utils.ExternalResource
import java.io.*
import java.util.*

 fun ExternalResource.toBase64File(): String {
     return inputStream().use {
         "base64://" + Base64.getEncoder().encodeToString(it.readBytes())
     }
 }

/**
 * 快速获取图片的大小
 *
 * @see https://www.cnblogs.com/xiaona/p/13869504.html
 * @author Mr.Er
 */
class FastImageInfo private constructor(
    val height: Int,
    val width: Int,
    val mimeType: String
) {
    override fun toString(): String = "FastImageInfo(mimeType=$mimeType,width=$width,height=$height)"
    companion object {
        operator fun invoke(file: File): FastImageInfo? {
            return FileInputStream(file).use { processStream(it) }
        }
        operator fun invoke(input: InputStream): FastImageInfo? {
            return input.use { processStream(it) }
        }
        operator fun invoke(bytes: ByteArray): FastImageInfo? {
            return ByteArrayInputStream(bytes).use { processStream(it) }
        }

        private fun processStream(input: InputStream): FastImageInfo? {
            val c1 = input.read()
            val c2 = input.read()
            var c3 = input.read()
            if (c1 == 'G'.code && c2 == 'I'.code && c3 == 'F'.code) { // GIF
                input.skip(3)
                val width = input.readInt(2, false)
                val height = input.readInt(2, false)
                val mimeType = "image/gif"
                return FastImageInfo(width, height, mimeType)
            } else if (c1 == 0xFF && c2 == 0xD8) { // JPG
                while (c3 == 255) {
                    val marker = input.read()
                    val len = input.readInt(2, true)
                    if (marker == 192 || marker == 193 || marker == 194) {
                        input.skip(1)
                        val height = input.readInt(2, true)
                        val width = input.readInt(2, true)
                        val mimeType = "image/jpeg"
                        return FastImageInfo(width, height, mimeType)
                    }
                    input.skip((len - 2).toLong())
                    c3 = input.read()
                }
            } else if (c1 == 137 && c2 == 80 && c3 == 78) { // PNG
                input.skip(15)
                val width = input.readInt(2, true)
                input.skip(2)
                val height = input.readInt(2, true)
                return FastImageInfo(width, height, "image/png")
            } else if (c1 == 66 && c2 == 77) { // BMP
                input.skip(15)
                val width = input.readInt(2, false)
                input.skip(2)
                val height = input.readInt(2, false)
                return FastImageInfo(width, height, "image/bmp")
            } else if (c1 == 'R'.code && c2 == 'I'.code && c3 == 'F'.code) { // WEBP
                val bytes = ByteArray(27)
                input.read(bytes)
                val width = bytes[24].toInt() and 0xff shl 8 or (bytes[23].toInt() and 0xff)
                val height = bytes[26].toInt() and 0xff shl 8 or (bytes[25].toInt() and 0xff)
                return FastImageInfo(width, height, "image/webp")
            } else {
                val c4 = input.read()
                if (c1 == 'M'.code && c2 == 'M'.code && c3 == 0 && c4 == 42 || c1 == 'I'.code && c2 == 'I'.code && c3 == 42 && c4 == 0) { //TIFF
                    val bigEndian = c1 == 'M'.code
                    val ifd: Int = input.readInt(4, bigEndian)
                    input.skip((ifd - 8).toLong())
                    val entries: Int = input.readInt(2, bigEndian)
                    var width = -1
                    var height = -1
                    for (i in 1..entries) {
                        val tag = input.readInt(2, bigEndian)
                        val fieldType = input.readInt(2, bigEndian)
                        var valOffset: Int
                        if (fieldType == 3 || fieldType == 8) {
                            valOffset = input.readInt(2, bigEndian)
                            input.skip(2)
                        } else {
                            valOffset = input.readInt(4, bigEndian)
                        }
                        if (tag == 256) {
                            width = valOffset
                        } else if (tag == 257) {
                            height = valOffset
                        }
                        if (width != -1 && height != -1) {
                            return FastImageInfo(width, height, "image/tiff")
                        }
                    }
                }
            }
            return null
        }

        @Throws(IOException::class)
        private fun InputStream.readInt(noOfBytes: Int, bigEndian: Boolean): Int {
            var ret = 0
            var sv = if (bigEndian) (noOfBytes - 1) * 8 else 0
            val cnt = if (bigEndian) -8 else 8
            for (i in 0 until noOfBytes) {
                ret = ret or (read() shl sv)
                sv += cnt
            }
            return ret
        }
    }
}

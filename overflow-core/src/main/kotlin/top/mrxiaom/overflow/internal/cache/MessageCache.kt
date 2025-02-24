package top.mrxiaom.overflow.internal.cache

import kotlinx.coroutines.launch
import net.mamoe.mirai.utils.MiraiExperimentalApi
import top.mrxiaom.overflow.OverflowAPI
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.message.data.WrappedAudio
import top.mrxiaom.overflow.internal.message.data.WrappedImage
import top.mrxiaom.overflow.internal.message.data.WrappedVideo
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(MiraiExperimentalApi::class)
internal object MessageCache {
    var enabled = false
    var saveDir: File = File("./cache")
    var keepDuration: Duration = Duration.INFINITE
    private val downloadQueue = ConcurrentLinkedQueue<Download>()
    private val lock = Object()

    internal fun cancelSchedule() {
        downloadQueue.clear()
    }

    internal fun scheduleDownload(image: WrappedImage) {
        if (!enabled) return
        val url = image.url
        if (url.shouldDownload()) {
            val parent = saveDir.resolve("images").also { if (!it.exists()) it.mkdirs() }
            synchronized(lock) {
                val file = parent.randomNewFile(".image")
                downloadQueue.offer(Download(url, file) {
                    image.url = it
                })
            }
            checkDownload()
        }
    }

    internal fun scheduleDownload(audio: WrappedAudio) {
        if (!enabled) return
        val url = audio.url
        if (url.shouldDownload()) {
            val parent = saveDir.resolve("audios").also { if (!it.exists()) it.mkdirs() }
            synchronized(lock) {
                val file = parent.randomNewFile(".audio")
                downloadQueue.offer(Download(url, file) {
                    audio.url = it
                })
            }
            checkDownload()
        }
    }

    internal fun scheduleDownload(video: WrappedVideo) {
        if (!enabled) return
        val url = video.file
        if (url.shouldDownload()) {
            val parent = saveDir.resolve("videos").also { if (!it.exists()) it.mkdirs() }
            synchronized(lock) {
                val file = parent.randomNewFile(".video")
                downloadQueue.offer(Download(url, file) {
                    val shouldUpdateVideoId = video.filename == video.videoId
                    video.file = it
                    video.filename = file.name
                    if (shouldUpdateVideoId) {
                        video.videoId = video.filename
                    }
                })
            }
            checkDownload()
        }
    }

    private fun String.shouldDownload(): Boolean {
        return startsWith("http://") || startsWith("https://")
    }

    private fun File.randomNewFile(ext: String): File {
        // 为了性能着想，就不获取后缀名了，ext 仅方便进行分类
        while(true) {
            val file = resolve(UUID.randomUUID().toString() + ext)
            if (file.exists() || downloadQueue.any { it.path.name == file.name }) {
                continue
            }
            return file
        }
    }

    private var downloading = false
    private fun checkDownload() {
        if (downloading || downloadQueue.isEmpty()) return
        downloading = true
        Overflow.instance.launch {
            val download = downloadQueue.poll()
            var tryCount = 3 // 重试次数
            while (tryCount-- > 0) try {
                URL(download.url).openConnection().apply {
                    // TODO: 设定请求参数
                    inputStream.use { input ->
                        FileOutputStream(download.path).use { output ->
                            var len: Int
                            val buffer = ByteArray(65536)
                            while (input.read(buffer).also { len = it } != -1) {
                                output.write(buffer, 0, len)
                            }
                        }
                    }
                }
                download.done()
                break
            } catch (e: Exception) {
                OverflowAPI.logger.warning("媒体消息缓存失败 ${download.url}", e)
            }
            downloading = false
            checkDownload()
        }
    }

    private var nextClean: Long = 0
    fun checkClean() {
        if (keepDuration == Duration.INFINITE) return
        val now = System.currentTimeMillis()
        if (now > nextClean) {
            // 至少隔1分钟清理一次
            nextClean = now + 60_000L
            doClean()
        }
    }

    fun doClean() {
        if (keepDuration == Duration.INFINITE) return
        val files = listOf(
            saveDir.resolve("images").listFiles(),
            saveDir.resolve("audios").listFiles(),
            saveDir.resolve("videos").listFiles()
        ).filterNotNull().flatMap { it.filter { it.isFile } }
        val now = System.currentTimeMillis()
        for (file in files) {
            val time = now - file.lastModified()
            if (time.milliseconds > keepDuration) {
                file.delete()
            }
        }
    }
}
internal class Download(
    val url: String,
    val path: File,
    val done: Consumer<String>,
) {
    fun done() {
        done.accept("file:///${path.absolutePath.removePrefix("/")}")
    }
}

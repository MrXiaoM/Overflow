package top.mrxiaom.overflow.internal.message.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.utils.toUHexString
import top.mrxiaom.overflow.internal.message.OnebotMessages.int
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import java.net.URL


internal suspend fun deserializeNeteaseMusic(id: String): MusicShare {
    return withContext(Dispatchers.IO) {
        val conn = URL("https://music.163.com/api/song/detail/?id=$id&ids=[$id]")
            .openConnection().also { it.connect() }
        val result = conn.inputStream.use {
            it.readBytes().toString(Charsets.UTF_8)
        }
        val songInfo = Json.parseToJsonElement(result).jsonObject["songs"]!!.jsonArray.first().jsonObject
        val title = songInfo["name"].string
        val singerName = songInfo["artists"]!!.jsonArray.first().jsonObject["name"].string
        val previewUrl = songInfo["album"]!!.jsonObject["picUrl"].string
        val playUrl = "https://music.163.com/song/media/outer/url?id=$id.mp3"
        val jumpUrl = "https://music.163.com/#/song?id=$id"
        MusicShare(MusicKind.NeteaseCloudMusic, title, singerName, jumpUrl, previewUrl, playUrl)
    }
}

internal suspend fun deserializeQQMusic(id: String): MusicShare {
    return withContext(Dispatchers.IO) {
        val conn = URL("https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq.json&needNewCode=0&data={%22comm%22:{%22ct%22:24,%22cv%22:0},%22songinfo%22:{%22method%22:%22get_song_detail_yqq%22,%22param%22:{%22song_type%22:0,%22song_mid%22:%22%22,%22song_id%22:$id},%22module%22:%22music.pf_song_detail_svr%22}}")
            .openConnection().also { it.connect() }
        val result = conn.inputStream.use {
            it.readBytes().toString(Charsets.UTF_8)
        }
        val songInfo = Json.parseToJsonElement(result).jsonObject["songinfo"]!!.jsonObject.takeIf { it["code"].int != 0 } ?: throw IllegalStateException("QQMusic code = 0")
        val data = songInfo["data"]!!.jsonObject
        val trackInfo = data["track_info"]!!.jsonObject
        val mid = trackInfo["mid"].string
        val previewMid = trackInfo["album"]!!.jsonObject["mid"].string
        val singerMid = (trackInfo["singer"] as? JsonArray)?.let {
            it[0].jsonObject["mid"]?.jsonPrimitive?.contentOrNull
        } ?: ""
        val title = trackInfo["title"].string
        val singerName = trackInfo["singer"]!!.jsonArray.first().jsonObject["name"].string
        val vs = (trackInfo["vs"] as? JsonArray)?.let {
            it[0].jsonPrimitive.contentOrNull
        } ?: ""
        val code = "${mid}q;z(&l~sdf2!nK".toByteArray().toUHexString("").substring(0 .. 4).uppercase()
        val playUrl = "http://c6.y.qq.com/rsc/fcgi-bin/fcg_pyq_play.fcg?songid=&songmid=$mid&songtype=1&fromtag=50&uin=&code=$code"
        val previewUrl = if (vs.isNotEmpty()) {
            "http://y.gtimg.cn/music/photo_new/T062R150x150M000$vs}.jpg"
        } else if (previewMid.isNotEmpty()) {
            "http://y.gtimg.cn/music/photo_new/T002R150x150M000$previewMid.jpg"
        } else if (singerMid.isNotEmpty()){
            "http://y.gtimg.cn/music/photo_new/T001R150x150M000$singerMid.jpg"
        } else {
            ""
        }
        val jumpUrl = "https://i.y.qq.com/v8/playsong.html?platform=11&appshare=android_qq&appversion=10030010&hosteuin=oKnlNenz7i-s7c**&songmid=${mid}&type=0&appsongtype=1&_wv=1&source=qq&ADTAG=qfshare"

        MusicShare(MusicKind.QQMusic, title, singerName, jumpUrl, previewUrl, playUrl)
    }
}

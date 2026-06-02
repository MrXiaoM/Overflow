package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.data.Location
import top.mrxiaom.overflow.message.BuildMessageContext

class LocationSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "location"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is Location
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val lat = data["lat"].string.toFloat()
        val lon = data["lon"].string.toFloat()
        val title = data["title"].string
        val content = data["content"].string
        context.add(Location(lat, lon, title, content))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? Location ?: return false
        context.add("location") {
            put("lat", element.lat.toString())
            put("lon", element.lon.toString())
            if (element.title.isNotEmpty()) put("title", element.title)
            if (element.content.isNotEmpty()) put("content", element.content)
        }
        return true
    }
}
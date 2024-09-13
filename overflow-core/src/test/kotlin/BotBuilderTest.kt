import cn.evolvefield.onebot.client.handler.ActionHandler
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.junit.jupiter.api.Test
import top.mrxiaom.overflow.BotBuilder
import java.lang.Exception
import java.net.InetSocketAddress
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

class BotBuilderTest {

    @Test
    fun `test positive connect`(): Unit = runBlocking {

        //TODO 需要实现模拟协议，不做。
        val client = suspendCoroutine<WebSocketServer> {
            val o: WebSocketServer = object : WebSocketServer(InetSocketAddress(3001)) {
                override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {

                }

                override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
                }

                override fun onMessage(conn: WebSocket?, message: String?) {
//                    {"action":"get_version_info","echo":0}
                    val json = Gson().fromJson(message, JsonObject::class.java)
                    val echo = json["echo"].asJsonPrimitive.asInt
                    conn!!.send(JsonObject().apply {
                        addProperty("status","ok")
                        addProperty("echo", echo)
                        add("data", JsonObject().apply {
                            addProperty("protocol_version", "v11")
                        })
                    }.toString())
                }

                override fun onError(conn: WebSocket?, ex: Exception?) {
                }

                override fun onStart() {
                    it.resume(this)
                }
            }
            o.start()
        }
        (1..3).map {
            println("尝试连接bot$it")
            val bot = BotBuilder.positive("ws://localhost:3001").connect()
            println("bot$it 已经连接到后端")
        }
    }

    @Test
    fun `test reversed connect`() {
    }
}
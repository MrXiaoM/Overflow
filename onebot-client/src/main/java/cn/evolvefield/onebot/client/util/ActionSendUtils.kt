package cn.evolvefield.onebot.client.util

import cn.evole.onebot.sdk.util.json.JsonsObject
import com.google.gson.JsonObject
import kotlinx.coroutines.CompletableDeferred
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:06
 * Version: 1.0
 */
/**
 * @param channel        [WebSocket]
 * @param requestTimeout Request Timeout
 */
class ActionSendUtils(
    private val channel: WebSocket,
    private val requestTimeout: Long
) {
    val resp = CompletableDeferred<JsonsObject>()
    //private var resp: JsonsObject? = null

    /**
     * @param req Request json data
     * @return Response json data
     * @throws IOException          exception
     * @throws InterruptedException exception
     */
    @Throws(IOException::class, InterruptedException::class)
    suspend fun send(req: JsonObject): JsonsObject {
        synchronized(channel) {
            log.debug(String.format("[Action] %s", req.toString()))
            channel.send(req.toString())
        }
        return resp.await()
        //synchronized(this) { this.wait(requestTimeout) }
        //return resp
    }

    /**
     * @param resp Response json data
     */
    fun onCallback(resp: JsonsObject) {
        this.resp.complete(resp)
        //this.resp = resp
        //synchronized(this) { this.notify() }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ActionSendUtils::class.java)
    }
}
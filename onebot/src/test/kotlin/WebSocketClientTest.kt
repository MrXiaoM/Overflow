import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evole.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.connection.ConnectFactory
import cn.evolvefield.onebot.client.handler.Handler
import cn.evolvefield.onebot.client.listener.SimpleEventListener
import cn.evolvefield.onebot.client.listener.impl.GroupMessageEventListener
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/2 19:06
 * Version: 1.0
 */
object WebSocketClientTest {
    val logger: Logger = LoggerFactory.getLogger("test")
    @JvmStatic
    suspend fun main(args: Array<String>) {
        val service = ConnectFactory.create(
            BotConfig("ws://127.0.0.1:8080")
        ) //创建websocket客户端
        val ws = service.createWebsocketClient() ?: throw IllegalStateException("未连接到 Onebot")
        val bot = ws.createBot()
        val dispatchers = ws.createEventBus() //创建事件分发器

        val groupMessageListener = GroupMessageEventListener() //自定义监听规则
        groupMessageListener.addHandler("test", object : Handler<GroupMessageEvent> {
            override suspend fun handle(e: GroupMessageEvent) {
                bot.sendGroupMsg(337631140, e.message, false)
            }
        }) //匹配关键字监听

        dispatchers.addListener(groupMessageListener) //注册监听

        dispatchers.addListener(object : SimpleEventListener<PrivateMessageEvent>() {
            //私聊监听
            override suspend fun onMessage(e: PrivateMessageEvent) {
                logger.info(e.toString())
            }
        }) //快速监听

        dispatchers.stop()
        ws.close()
    }

}
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.junit.jupiter.api.Test
import top.mrxiaom.overflow.BotBuilder

class OverFlowTest {
    @Test
    fun `should be reconnect successfully`(): Unit = runBlocking {
        val builder = BotBuilder.reversed(3001)

        while (true) {
            println("等待连接中...")
            val bot = builder.connect()
            if (bot !== null) {
                println("新bot加入连接:${bot}")
                //确保重新连接后event可以正常接收
                //这里建议使用GlobalEventChannel避免重复注册的问题
                bot.eventChannel.subscribeAlways<GroupMessageEvent> {
                    println(it)
                }
            }
        }
    }
}
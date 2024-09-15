import kotlinx.coroutines.runBlocking
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
            }
        }
    }
}
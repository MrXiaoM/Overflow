import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.messageChainOf
import org.junit.jupiter.api.Test
import top.mrxiaom.overflow.internal.message.OnebotMessages
import kotlin.test.assertEquals

class MessageSerializationTest {
    @Test
    fun serializesAtAll() {
        OnebotMessages.registerSerializers()
        val message = messageChainOf(AtAll)

        val encoded = with(MessageChain) { message.serializeToJsonString() }
        val decoded = MessageChain.deserializeFromJsonString(encoded)

        assertEquals(message, decoded)
    }
}

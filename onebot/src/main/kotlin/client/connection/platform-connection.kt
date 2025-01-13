package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.core.Bot
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

interface OneBotProducer {
    fun invokeOnClose(block: () -> Unit)
    fun close()
    fun setBotConsumer(consumer: suspend (Bot) -> Unit)
    suspend fun awaitNewBotConnection(duration: Duration = Duration.INFINITE): Bot?
}

class PositiveOneBotProducer(
    private val client: WSClient
): OneBotProducer {
    override fun invokeOnClose(block: () -> Unit) = TODO("客户端暂不支持断线重连")

    override fun close() = client.close()

    override fun setBotConsumer(consumer: suspend (Bot) -> Unit) {
        client.botConsumer = consumer
    }

    override suspend fun awaitNewBotConnection(duration: Duration): Bot? {
        return kotlin.runCatching {
            withTimeout(duration) {
                if (client.connectSuspend()) client.createBot() else null
            }
        }.getOrNull()
    }
}

class ReversedOneBotProducer(
    private val server: WSServer
): OneBotProducer {
    override fun invokeOnClose(block: () -> Unit) {
        server.closeHandler.add(block)
    }

    override fun close() = server.stop()

    override fun setBotConsumer(consumer: suspend (Bot) -> Unit) {
        server.botConsumer = consumer
    }

    override suspend fun awaitNewBotConnection(duration: Duration): Bot? {
        return kotlin.runCatching {
            withTimeout(duration) {
                server.awaitNewBot()
            }
        }.getOrNull()
    }
}
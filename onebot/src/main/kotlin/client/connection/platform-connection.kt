package cn.evolvefield.onebot.client.connection

import cn.evolvefield.onebot.client.core.Bot
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

interface OneBotProducer {
    suspend fun awaitNewBotConnection(duration: Duration = Duration.INFINITE): Bot?
}

class PositiveOneBotProducer(private val client: WSClient) : OneBotProducer {
    override suspend fun awaitNewBotConnection(duration: Duration): Bot? {
        return kotlin.runCatching {
            withTimeout(duration) {
                if (client.connectSuspend()) client.createBot() else null
            }
        }.getOrNull()
    }
}

class ReversedOneBotProducer(private val server: WSServer) : OneBotProducer {
    override suspend fun awaitNewBotConnection(duration: Duration): Bot? {
        return kotlin.runCatching {
            withTimeout(duration) {
                server.awaitNewBot()
            }
        }.getOrNull()
    }
}
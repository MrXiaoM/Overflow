package client.connection

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.connection.ConnectFactory
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class OneBotProducerTest {

    @Test
    fun `positive connection should be connect once`(): Unit = runBlocking {
        val job1 = launch {
            ConnectFactory.create(
                BotConfig(
                    reversedPort = 3001
                )
            ).createProducer()
            delay(30.seconds)
        }
        delay(1.seconds)
        val botConfig = BotConfig(
            url = "ws://localhost:3001",
        )
        val service = ConnectFactory.create(botConfig, botConfig.parentJob).createProducer(this)

        val conn = service.awaitNewBotConnection(duration = 5.seconds)
        assertNotNull(conn)
        println("连接：${conn}")
        assertNull(service.awaitNewBotConnection())
        job1.cancel()
    }

    @Test
    fun `reversed connection should be connect success`(): Unit = runBlocking(Dispatchers.IO) {
        val server = ConnectFactory.create(
            BotConfig(
                reversedPort = 3001
            )
        ).createProducer()


        val jobs = launch {
            delay(1.seconds)
            val botConfig = BotConfig(
                url = "ws://localhost:3001",
            )
            val mockBotInstance = ConnectFactory.create(botConfig, botConfig.parentJob)
            for (i in 1..3) {
                mockBotInstance.createProducer(this).awaitNewBotConnection().apply {
                    println("连接被处理：${this}")
                }
                delay(1.seconds)
            }
        }

        launch {
            repeat(3) {
                val receivedBot = server.awaitNewBotConnection()
                println("有新连接: $receivedBot")
            }
            jobs.cancel()
        }
        jobs.join()
    }
    @Test
    fun `should be called when server closing`(): Unit = runBlocking {
        val factory = ConnectFactory.create(
            BotConfig(
                reversedPort = 3001
            )
        ).createProducer()

        val defer = CompletableDeferred<Unit>()
        factory.invokeOnClose {
            defer.complete(Unit)
        }
        launch {
            delay(3.seconds)
            factory.close()
        }
        defer.join()
    }
}
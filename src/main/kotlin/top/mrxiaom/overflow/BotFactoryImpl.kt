package top.mrxiaom.overflow

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.utils.BotConfiguration
import top.mrxiaom.overflow.contact.BotWrapper

object BotFactoryImpl : BotFactory {
    override fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot {
        TODO("Not yet implemented")
    }
    internal var internalBot: cn.evolvefield.onebot.client.core.Bot? = null
    val bot: cn.evolvefield.onebot.client.core.Bot
        get() = internalBot!!

    override fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot {
        TODO("Not yet implemented")
    }

    override fun newBot(qq: Long, authorization: BotAuthorization, configuration: BotConfiguration): Bot {
        TODO("Not yet implemented")
    }
}}

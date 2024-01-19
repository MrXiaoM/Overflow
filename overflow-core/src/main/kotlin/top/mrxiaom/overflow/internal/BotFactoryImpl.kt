package top.mrxiaom.overflow.internal

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.utils.BotConfiguration
import top.mrxiaom.overflow.internal.contact.BotWrapper

internal object BotFactoryImpl : BotFactory {
    internal var internalBot: cn.evolvefield.onebot.client.core.Bot? = null
    val bot: cn.evolvefield.onebot.client.core.Bot
        get() = internalBot!!

    override fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot = end(qq, configuration)

    override fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot = end(qq, configuration)

    override fun newBot(qq: Long, authorization: BotAuthorization, configuration: BotConfiguration): Bot = end(qq, configuration)

    private fun end(qq: Long, configuration: BotConfiguration): Bot {
        if (internalBot != null) runBlocking {
            val data = internalBot!!.getLoginInfo().data
            if (data.userId == qq) {
                Bot.getInstanceOrNull(qq) ?: BotWrapper.wrap(internalBot!!, configuration)
            } else null
        }?.also { return it }
        throw UnsupportedOperationException("溢出核心已委托远程实现接管了账户管理，mirai 框架端没有登录机器人的职责")
    }
}

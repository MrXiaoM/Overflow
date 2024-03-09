package top.mrxiaom.overflow.internal

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.utils.BotConfiguration

internal object BotFactoryImpl : BotFactory {
    override fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot = end(qq)

    override fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot = end(qq)

    override fun newBot(qq: Long, authorization: BotAuthorization, configuration: BotConfiguration): Bot = end(qq)

    private fun end(qq: Long): Bot {
        Bot.getInstanceOrNull(qq)?.also { return it }
        throw UnsupportedOperationException("溢出核心已委托远程实现接管了账户管理，mirai 框架端没有登录机器人的职责")
    }
}

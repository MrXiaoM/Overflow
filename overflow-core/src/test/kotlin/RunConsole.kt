import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@OptIn(ConsoleExperimentalApi::class, ConsoleFrontEndImplementation::class)
suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon(MiraiConsoleImplementationTerminal())
    MiraiConsole.job.join()
}
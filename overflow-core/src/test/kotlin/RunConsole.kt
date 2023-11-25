import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.io.File

@OptIn(ConsoleExperimentalApi::class, ConsoleFrontEndImplementation::class)
suspend fun main() {
    val runDir = File("run")
    if (!runDir.exists()) runDir.mkdirs()

    MiraiConsoleTerminalLoader.startAsDaemon(MiraiConsoleImplementationTerminal(runDir.toPath()))
    MiraiConsole.job.join()
}
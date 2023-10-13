import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.io.File

@OptIn(ConsoleExperimentalApi::class)
suspend fun main() {
    val runDir = File("run")
    if (!runDir.exists()) runDir.mkdirs()
    MiraiConsoleTerminalLoader.startAsDaemon(MiraiConsoleImplementationTerminal(runDir.toPath()))
    net.mamoe.mirai.console.enduserreadme.EndUserReadme
    MiraiConsole.job.join()
}
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

fun <T> T.ext(name: String): T {
    return also { Helper.rootProject.extra[name] = it }
}

inline fun <reified T> Project.extra(name: String): T? {
    return rootProject.extra[name].run {
        if (this is T) this else null
    }
}
object Helper {
    internal lateinit var rootProj: Project
    val rootProject: Project
        get() = rootProj
}
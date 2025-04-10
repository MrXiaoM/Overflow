import buildSrc.BuildConstants
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import java.util.*

fun <T> T.ext(name: String): T {
    return also { Helper.rootProject.extra[name] = it }
}

inline fun <reified T> Project.extra(name: String): T? {
    return rootProject.extra[name].run {
        if (this is T) this else null
    }
}
object Helper {
    @Suppress("MemberVisibilityCanBePrivate")
    internal lateinit var proj: Project
    val rootProject: Project
        get() = proj
}

val prop = Properties().apply {
    BuildConstants.PROPERTIES_FILE
        .reader().use(::load)
}

fun prop(name: String): String {
    return prop[name]?.toString() ?: ""
}

fun Project.optInForAllSourceSets(qualifiedClassname: String) {
    kotlinSourceSets!!.all {
        languageSettings {
            optIn(qualifiedClassname)
        }
    }
}
inline fun <reified T> Any?.safeAs(): T? {
    return this as? T
}
val Project.kotlinSourceSets get() = extensions.findByName("kotlin").safeAs<KotlinProjectExtension>()?.sourceSets

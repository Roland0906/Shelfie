import com.rolandlin.shelfie.buildlogic.SHELFIE_JAVA_VERSION
import com.rolandlin.shelfie.buildlogic.configureKotlinCompile
import com.rolandlin.shelfie.buildlogic.lib
import com.rolandlin.shelfie.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Modules that don't need the Android framework stay plain JVM: their tests run
 * without Android tooling, and moving them to KMP commonMain later needs no untangling.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = SHELFIE_JAVA_VERSION
            targetCompatibility = SHELFIE_JAVA_VERSION
        }
        configureKotlinCompile()

        dependencies {
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("kotlin-test"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
            add("testImplementation", libs.lib("turbine"))
        }
    }
}

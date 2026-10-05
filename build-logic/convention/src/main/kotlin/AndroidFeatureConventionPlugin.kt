import com.rolandlin.shelfie.buildlogic.lib
import com.rolandlin.shelfie.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module is presentation (ViewModel + UI state) plus Compose UI. Its dependency
 * set is defined only here, so "what a feature can see" has a single definition.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("shelfie.android.library")
        pluginManager.apply("shelfie.android.compose")

        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":core:common"))
            add("implementation", project(":core:data"))
            add("implementation", project(":core:designsystem"))

            add("implementation", libs.lib("androidx-compose-material3"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", platform(libs.lib("koin-bom")))
            add("implementation", libs.lib("koin-androidx-compose"))

            add("testImplementation", project(":core:testing"))
        }
    }
}

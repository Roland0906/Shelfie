import com.android.build.api.dsl.LibraryExtension
import com.rolandlin.shelfie.buildlogic.SHELFIE_JAVA_VERSION
import com.rolandlin.shelfie.buildlogic.ShelfieSdk
import com.rolandlin.shelfie.buildlogic.configureKotlinCompile
import com.rolandlin.shelfie.buildlogic.enforceModuleRules
import com.rolandlin.shelfie.buildlogic.lib
import com.rolandlin.shelfie.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            // :core:data -> com.rolandlin.shelfie.core.data
            namespace = "com.rolandlin.shelfie" + path.replace(':', '.')
            compileSdk = ShelfieSdk.COMPILE
            defaultConfig {
                minSdk = ShelfieSdk.MIN
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = SHELFIE_JAVA_VERSION
                targetCompatibility = SHELFIE_JAVA_VERSION
            }
            testOptions.unitTests.isReturnDefaultValues = true
        }
        configureKotlinCompile()
        enforceModuleRules()

        dependencies {
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("kotlin-test"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
            add("testImplementation", libs.lib("turbine"))
        }
    }
}

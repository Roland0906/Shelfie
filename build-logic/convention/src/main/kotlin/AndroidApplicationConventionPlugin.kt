import com.android.build.api.dsl.ApplicationExtension
import com.rolandlin.shelfie.buildlogic.SHELFIE_JAVA_VERSION
import com.rolandlin.shelfie.buildlogic.ShelfieSdk
import com.rolandlin.shelfie.buildlogic.configureKotlinCompile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk = ShelfieSdk.COMPILE
            defaultConfig {
                minSdk = ShelfieSdk.MIN
                targetSdk = ShelfieSdk.TARGET
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = SHELFIE_JAVA_VERSION
                targetCompatibility = SHELFIE_JAVA_VERSION
            }
        }
        configureKotlinCompile()
    }
}

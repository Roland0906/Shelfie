import androidx.room.gradle.RoomExtension
import com.rolandlin.shelfie.buildlogic.lib
import com.rolandlin.shelfie.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("androidx.room")

        // Schema JSON is version controlled so migrations can be verified against it
        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        dependencies {
            add("implementation", libs.lib("androidx-room-runtime"))
            add("ksp", libs.lib("androidx-room-compiler"))
        }
    }
}

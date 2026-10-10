plugins {
    alias(libs.plugins.shelfie.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

// The HTTP contract between the app and :server. Plain JVM so both sides compile against it.
dependencies {
    api(projects.core.model)
    api(libs.kotlinx.serialization.json)
}

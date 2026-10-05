plugins {
    alias(libs.plugins.shelfie.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.core.common)
    api(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.ktor.client.mock)
}

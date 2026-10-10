plugins {
    alias(libs.plugins.shelfie.jvm.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("com.rolandlin.shelfie.server.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("shelfie-server.jar")
    }
}

dependencies {
    implementation(projects.core.contract)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
}

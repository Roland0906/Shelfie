plugins {
    alias(libs.plugins.shelfie.android.library)
    alias(libs.plugins.shelfie.android.room)
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.room.runtime)
    api(libs.androidx.paging.common)

    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}

plugins {
    alias(libs.plugins.shelfie.android.library)
}

dependencies {
    // Features see only model, common and paging. Network and database are implementation
    // dependencies, so DAOs and API clients are not on a feature's compile classpath.
    api(projects.core.model)
    api(projects.core.common)
    api(libs.androidx.paging.common)

    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}

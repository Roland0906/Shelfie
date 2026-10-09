plugins {
    alias(libs.plugins.shelfie.android.library)
    alias(libs.plugins.shelfie.android.compose)
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(projects.core.common)
    implementation(projects.core.model)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}

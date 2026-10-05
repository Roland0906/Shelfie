plugins {
    alias(libs.plugins.shelfie.android.feature)
}

dependencies {
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    testImplementation(libs.androidx.paging.testing)
}

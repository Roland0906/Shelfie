plugins {
    alias(libs.plugins.shelfie.android.library)
}

// Fake repositories and shared rules for feature tests; fakes over mocks keep tests behavioral.
dependencies {
    api(projects.core.data)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.androidx.paging.common)
}

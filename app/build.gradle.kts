plugins {
    alias(libs.plugins.shelfie.android.application)
    alias(libs.plugins.shelfie.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rolandlin.shelfie"

    defaultConfig {
        applicationId = "com.rolandlin.shelfie"
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // The app is the only module that sees everything: it wires DI and cross-feature navigation
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.feature.search)
    implementation(projects.feature.bookdetail)
    implementation(projects.feature.shelf)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

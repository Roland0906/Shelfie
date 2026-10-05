package com.rolandlin.shelfie.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal object ShelfieSdk {
    const val COMPILE = 37
    const val MIN = 24
    const val TARGET = 36
}

internal val SHELFIE_JAVA_VERSION = JavaVersion.VERSION_17

/** Kotlin compiler settings shared by Android and plain JVM modules. */
internal fun Project.configureKotlinCompile() {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(providers.gradleProperty("warningsAsErrors").map(String::toBoolean).orElse(false))
        }
    }
}

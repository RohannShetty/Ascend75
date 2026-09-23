plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

// Deliberately no Android plugin: this module must stay testable on a bare JVM.
// No jvmToolchain(17) on purpose -- it would try to auto-provision a JDK; the build JDK is 17 already.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}

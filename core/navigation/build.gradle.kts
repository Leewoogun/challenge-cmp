plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

android {
    namespace = "com.lwg.challenge.navigation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

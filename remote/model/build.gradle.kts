plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

android {
    namespace = "com.lwg.challenge.remote.model"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

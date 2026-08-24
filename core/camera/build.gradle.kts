plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.camera"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // PhotoCompressor 실패 원인 Logger
            implementation(projects.core.utils)
        }
        androidMain.dependencies {
            // FileProvider
            implementation(libs.androidx.core.ktx)
            // rememberLauncherForActivityResult
            implementation(libs.androidx.activity.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

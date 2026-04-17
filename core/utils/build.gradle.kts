plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.utils"

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {

        }
        commonMain.dependencies {
            implementation(libs.kermit)
        }
        iosMain.dependencies {

        }
    }
}

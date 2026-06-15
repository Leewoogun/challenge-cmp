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
            implementation(libs.kotlinx.coroutines.core)
            // home-feed 상대 시간 헬퍼(InstantFormat)는 stdlib 의 kotlin.time API 만 사용 — 추가 의존성 불필요.
        }
        iosMain.dependencies {

        }
    }
}

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
            // 서버 시간 규약(KST, `yyyy-MM-dd HH:mm:ss`)의 LocalDateTime 파싱과 타임존 산술에 사용
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {

        }
    }
}

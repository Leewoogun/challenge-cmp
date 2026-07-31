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
            // 시간 필드가 공개 타입(LocalDate/LocalDateTime)으로 노출되므로 implementation 이 아니라 api.
            api(libs.kotlinx.datetime)
            // wire 포맷(yyyy-MM-dd HH:mm:ss) 정의 - ChallengeDateTimeFormats
            implementation(projects.core.utils)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

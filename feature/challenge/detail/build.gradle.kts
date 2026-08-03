plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.challenge.detail"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Refresh 아이콘 직접 import.
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // 테스트 픽스처가 ChallengeDetail 의 LocalDate/LocalDateTime 을 직접 만든다.
            implementation(libs.kotlinx.datetime)
        }
    }
}

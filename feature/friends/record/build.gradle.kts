plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.friends.record"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SportsKabaddi·CloudOff·Refresh 를 직접 import 한다.
            implementation(compose.materialIconsExtended)
            // UiState·프리뷰 픽스처가 LocalDateTime 을 직접 import 한다
            // (:domain:model 이 api 로 갖고 있으나 android 타깃으로 전이되지 않음 — mypage 선례).
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

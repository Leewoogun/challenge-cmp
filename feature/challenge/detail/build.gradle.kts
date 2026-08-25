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
            // 인증 사진 로딩. Fetcher(coil.network)는 composeApp 이 인증 HttpClient 로 이미 배선해
            // 싱글턴 ImageLoader 에 심었으므로 여기선 Compose API 만 쓴다.
            implementation(libs.coil)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // 테스트 픽스처가 ChallengeDetail 의 LocalDate/LocalDateTime 을 직접 만든다.
            implementation(libs.kotlinx.datetime)
        }
    }
}

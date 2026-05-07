plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.login"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // 카카오 OAuth (Android) — App-to-App + 카카오 계정 웹 fallback 까지 SDK 가 처리.
            // SDK 초기화는 ChallengeApplication.onCreate, AuthCodeHandlerActivity 는 AndroidManifest 에 등록.
            implementation(libs.kakao.sdk.user)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

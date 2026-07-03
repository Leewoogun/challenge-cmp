plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.challenge.core.invite"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // KakaoInviter 플랫폼 구현 DI(expect/actual platformInviteModule)용 Koin DSL.
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            // 카카오톡 공유(KakaoLink) — SharingResult.intent 로 카톡 공유 시트 진입.
            // SDK 초기화(KakaoSdk.init) 는 ChallengeApplication.onCreate 에서 이미 수행.
            implementation(libs.kakao.sdk.share)
        }
    }
}

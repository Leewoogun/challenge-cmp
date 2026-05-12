plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api 로 노출 — feature 모듈이 :core:ui 만 의존해도 :core:designsystem 도 사용 가능
            api(projects.core.designsystem)
        }
    }
}

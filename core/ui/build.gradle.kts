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
            // SignatureField 가 Refresh 아이콘을 직접 import 한다.
            implementation(compose.materialIconsExtended)
            // SignatureCanvas / SignatureView 가 도메인 모델 Signature 를 직접 받는다.
            // Clean Arch 방향(UI → Domain)에 맞고 :domain:model 은 순수 Kotlin 이라 순환이 없다.
            implementation(projects.domain.model)
        }
        androidMain.dependencies {
            // PlatformBackHandler 의 android actual 이 BackHandler 를,
            // NotificationPermissionEffect 가 rememberLauncherForActivityResult 를 쓴다.
            implementation(libs.androidx.activity.compose)
            // NotificationPermissionEffect 의 ContextCompat.checkSelfPermission.
            // activity-compose 를 타고 전이로도 들어오지만, 직접 쓰는 API 라 :core:designsystem 처럼 명시한다.
            implementation(libs.androidx.core.ktx)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

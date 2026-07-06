import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
    alias(libs.plugins.buildkonfig)
}

android {
    namespace = "com.lwg.challenge.core.invite"
}

// local.properties 의 kakao_invite_template_id → BuildKonfig.KAKAO_INVITE_TEMPLATE_ID.
// 카카오 콘솔 [메시지 템플릿 도구] 에서 만든 커스텀 템플릿 ID (composeApp 의 앱 키 주입과 동일 패턴).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val kakaoInviteTemplateId: String =
    localProperties["kakao_invite_template_id"]?.toString()?.trim('"').orEmpty()

buildkonfig {
    packageName = "com.lwg.challenge.core.invite"
    defaultConfigs {
        buildConfigField(STRING, "KAKAO_INVITE_TEMPLATE_ID", kakaoInviteTemplateId)
    }
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

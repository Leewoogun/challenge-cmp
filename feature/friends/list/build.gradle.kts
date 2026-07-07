import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.challengeFeature)
    alias(libs.plugins.buildkonfig)
}

android {
    namespace = "com.lwg.challenge.feature.friends.list"
}

// local.properties 의 kakao_invite_template_id → BuildKonfig.KAKAO_INVITE_TEMPLATE_ID.
// 카카오 콘솔 [메시지 템플릿 도구] 에서 만든 친구 초대 커스텀 템플릿 ID.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val kakaoInviteTemplateId: String =
    localProperties["kakao_invite_template_id"]?.toString()?.trim('"').orEmpty()

buildkonfig {
    packageName = "com.lwg.challenge.feature.friends.list"
    defaultConfigs {
        buildConfigField(STRING, "KAKAO_INVITE_TEMPLATE_ID", kakaoInviteTemplateId)
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // FriendsScreen 에서 Icons.Filled.Group 사용.
            // (:core:designsystem 도 materialIconsExtended 를 commonMain 의 implementation 으로 갖지만,
            //  Feature 모듈에서 직접 import 하므로 명시 선언.)
            implementation(compose.materialIconsExtended)
        }
        androidMain.dependencies {
            // 카카오톡 공유(KakaoLink) — SharingResult.intent 로 카톡 공유 시트 진입.
            // SDK 초기화(KakaoSdk.init) 는 ChallengeApplication.onCreate 에서 이미 수행.
            implementation(libs.kakao.sdk.share)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

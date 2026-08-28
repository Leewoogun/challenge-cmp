import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.challengeKspKoin)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.buildkonfig)
}

android {
    namespace = "com.lwg.challenge.remote.network"
}

val properties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

buildkonfig {
    packageName = "com.lwg.challenge.remote.network"

    // 기본 생성물은 internal 이라 모듈 밖에서 안 보인다. photoUrl 절대 URL 조립을 위해
    // :remote:datasource 가 BASE_URL 을 읽어야 해서 public 으로 노출한다.
    exposeObjectWithName = "BuildKonfig"

    // 기본값: Android 에뮬레이터용 URL. (ADR-0007)
    // Android target 은 defaultConfigs 를 그대로 사용하고, iOS target 들은 targetConfigs 로 오버라이드.
    defaultConfigs {
        buildConfigField(
            STRING,
            "BASE_URL",
            properties["challenge_api_base_url_android"]?.toString()?.trim('"')
                ?: "http://10.0.2.2:8080/",
        )
    }

    val iosBaseUrl = properties["challenge_api_base_url_ios"]?.toString()?.trim('"')
        ?: "http://localhost:8080/"

    targetConfigs {
        create("iosArm64") {
            buildConfigField(STRING, "BASE_URL", iosBaseUrl)
        }
        create("iosSimulatorArm64") {
            buildConfigField(STRING, "BASE_URL", iosBaseUrl)
        }
        create("iosX64") {
            buildConfigField(STRING, "BASE_URL", iosBaseUrl)
        }
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            // Remote model (BaseResponse envelope)
            implementation(projects.remote.model)

            // Ktorfit
            implementation(libs.ktorfit.lib)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)

            // Utils (Logger)
            implementation(projects.core.utils)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // HttpNetworkLogger 가 이미지 응답 스트림을 소비하지 않는지 검증하려면 실제 HttpClient 파이프라인이 필요하다.
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.mock)
            // ChallengeAuthTest 는 Auth 플러그인의 실제 401 재시도 경로를 태우므로
            // refresh 응답 파싱용 ContentNegotiation 까지 프로덕션과 동일하게 필요하다.
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(projects.core.utils)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.challengeKspKoin)
    alias(libs.plugins.ktorfit)
}

android {
    namespace = "com.lwg.challenge.remote.network"
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

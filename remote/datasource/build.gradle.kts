plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeKspKoin)
}

android {
    namespace = "com.lwg.challenge.remote.datasource"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DataSource 인터페이스 (도메인 모델은 api 로 전이됨)
            implementation(projects.data.datasource)

            // Remote
            implementation(projects.remote.api)
            implementation(projects.remote.model)
            implementation(projects.remote.mapper)
            implementation(projects.remote.network)

            // Constants.BASE_URL
            implementation(projects.core.utils)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // multipart 본문(MultiPartFormDataContent) 조립
            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            // multipart 회귀 테스트가 운영과 같은 파이프라인(DefaultRequest+ContentNegotiation)을
            // 실제 Ktorfit 으로 재현한다
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktorfit.lib)
        }
    }
}

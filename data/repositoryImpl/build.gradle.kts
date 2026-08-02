plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeKspKoin)
}

android {
    namespace = "com.lwg.challenge.data.repositoryimpl"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain
            implementation(projects.domain.model)
            implementation(projects.domain.repository)
            implementation(projects.domain.usecase)

            // Local
            implementation(projects.local.datastore)

            // Remote
            implementation(projects.remote.api)
            implementation(projects.remote.model)
            implementation(projects.remote.mapper)
            implementation(projects.remote.network)

            // Utils (AuthEventBus)
            implementation(projects.core.utils)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            // AuthTokenCacheInvalidator 는 HttpClient 를 요구하는 final 클래스라 fake 가 불가능해
            // MockEngine 클라이언트로 실물을 생성한다 (Auth 플러그인이 없어 invalidate 는 no-op)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.mock)
        }
    }
}

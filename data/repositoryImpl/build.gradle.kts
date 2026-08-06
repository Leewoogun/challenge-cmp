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

            // DataSource (Repository 가 의존하는 유일한 데이터 경로)
            implementation(projects.data.datasource)

            // DataSource 구현 모듈 — DataModule 이 Koin 모듈로 include 하기 위해서만 필요
            implementation(projects.local.datastore)
            implementation(projects.remote.datasource)

            // AuthTokenCacheInvalidator
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

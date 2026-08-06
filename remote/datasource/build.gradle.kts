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
        }
    }
}

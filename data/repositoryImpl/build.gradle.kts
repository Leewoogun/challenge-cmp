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
    }
}

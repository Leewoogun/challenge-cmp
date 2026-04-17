plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeKspKoin)
    alias(libs.plugins.kotlinxSerialization)
}

android {
    namespace = "com.lwg.challenge.local.datastore"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain.model)
            implementation(projects.domain.repository)
            implementation(projects.core.utils)

            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
    }
}

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

android {
    namespace = "com.lwg.challenge.remote.mapper"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain.model)
            implementation(projects.remote.model)
            // SignatureCodec 의 저장 포맷 직렬화
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

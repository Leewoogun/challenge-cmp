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
            // Ktorfit
            implementation(libs.ktorfit.lib)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)

            // Utils (Logger)
            implementation(projects.core.utils)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

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

    defaultConfigs {
        buildConfigField(STRING, "TMDB_TOKEN", properties["tmdb_token"].toString().trim('"'))
        buildConfigField(STRING, "BASE_URL", "https://api.themoviedb.org/3/")
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

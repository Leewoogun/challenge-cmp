plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeKspKoin)
    alias(libs.plugins.room)
}

android {
    namespace = "com.lwg.challenge.local.database"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain.model)
            implementation(projects.domain.repository)
            implementation(projects.core.utils)

            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf(
        "kspAndroid",
        "kspIosSimulatorArm64",
        "kspIosArm64",
    ).forEach { target ->
        add(target, libs.room.compiler)
    }
}

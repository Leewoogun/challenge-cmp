plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeKspKoin)
    alias(libs.plugins.ktorfit)
}

android {
    namespace = "com.lwg.challenge.remote.api"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.remote.network)
            implementation(projects.remote.model)

            // Ktorfit
            implementation(libs.ktorfit.lib)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.ktorfit.ksp)
    add("kspAndroid", libs.ktorfit.ksp)
    add("kspIosArm64", libs.ktorfit.ksp)
    add("kspIosSimulatorArm64", libs.ktorfit.ksp)
}

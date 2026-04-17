plugins {
    alias(libs.plugins.challengeKotlinMultiplatformPure)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain.model)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

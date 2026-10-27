plugins {
    alias(libs.plugins.challengeKotlinMultiplatformPure)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain.model)
            implementation(projects.domain.repository)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

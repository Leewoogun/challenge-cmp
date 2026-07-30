plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.challenge.create"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ArrowBack / KeyboardArrowRight / Schedule / SportsKabaddi / Group / PersonAdd / Refresh 직접 import.
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

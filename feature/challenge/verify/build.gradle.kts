plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.challenge.verify"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // PhotoCamera 아이콘 직접 import.
            implementation(compose.materialIconsExtended)
            implementation(projects.core.camera)
            implementation(projects.core.permission)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

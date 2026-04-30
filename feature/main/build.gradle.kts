plugins {
    alias(libs.plugins.challengeFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)

            implementation(projects.feature.home)
            implementation(projects.feature.login)
            implementation(projects.feature.splash)
            implementation(projects.feature.ex1)
            implementation(projects.feature.ex2)
            implementation(projects.feature.ex3)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "com.lwg.challenge.feature.main"
}

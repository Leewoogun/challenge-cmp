plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.friends.search"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // FriendSearchItem 에서 Icons.Filled.PersonAdd/Refresh 등 사용.
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

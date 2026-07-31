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
            // ViewModel / Preview 에서 LocalDateTime 을 직접 import 하므로 명시 선언 (:domain:model 이 api 로 갖고 있으나 android 타깃으로 전이되지 않음).
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

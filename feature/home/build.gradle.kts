plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.home"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // HomeTopBar / HomeScreen 에서 Notifications, LocalFireDepartment, Add 등 사용.
            // (:core:designsystem 도 materialIconsExtended 를 commonMain 의 implementation 으로 갖지만,
            //  Feature 모듈에서 직접 import 하므로 명시 선언.)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

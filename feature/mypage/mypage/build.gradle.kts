plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.mypage.mypage"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // MyPageMenuCard / MyPageScreen 이 Material 확장 아이콘을 직접 import 한다.
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

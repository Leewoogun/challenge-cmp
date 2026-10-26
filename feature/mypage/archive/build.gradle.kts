plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.mypage.archive"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 보관함 화면·컴포넌트가 Material 확장 아이콘을 직접 import 한다.
            implementation(compose.materialIconsExtended)
            // State·프리뷰 픽스처가 LocalDate 를 직접 import 한다
            // (:domain:model 이 api 로 갖고 있으나 android 타깃으로 전이되지 않음).
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.mypage"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // MyPageMenuCard / MyPageScreen / 보관함에서 Description, Logout,
            // SportsKabaddi, LocalFireDepartment 를 사용.
            // (:core:designsystem 도 materialIconsExtended 를 commonMain 의 implementation 으로 갖지만,
            //  Feature 모듈에서 직접 import 하므로 명시 선언.)
            implementation(compose.materialIconsExtended)
            // 보관함 State·프리뷰 픽스처가 LocalDate 를 직접 import 한다
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

plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "com.lwg.challenge.feature.notification"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 타입별 아이콘(SportsKabaddi, Description, Cancel, NotificationsNone)을 직접 import 한다.
            implementation(compose.materialIconsExtended)
            // 목적지 매핑(PushEvent.of + toRoute)을 푸시 수신과 공유한다 — 사본을 만들면 같은 알림이
            // 푸시와 목록에서 다른 화면으로 간다.
            implementation(projects.core.push)
            // ItemState·프리뷰 픽스처가 LocalDateTime 을 직접 import 한다
            // (:domain:model 이 api 로 갖고 있으나 android 타깃으로 전이되지 않음 — mypage 선례).
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

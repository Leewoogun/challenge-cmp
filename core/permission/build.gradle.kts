plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.permission"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // moko 는 이 모듈 밖으로 새지 않는다 — 호출부는 AppPermission / PermissionStatus 만 본다.
            implementation(libs.moko.permissions)
            // PermissionManager.Bind() 의 BindEffect
            implementation(libs.moko.permissions.compose)
            // Permission 상수는 권한별 아티팩트로 쪼개져 있다. 새 권한을 쓰면 여기에 추가한다.
            implementation(libs.moko.permissions.notifications)
            implementation(libs.moko.permissions.camera)

            // platformPermissionModule 이 Koin Module 을 수동 구성한다.
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

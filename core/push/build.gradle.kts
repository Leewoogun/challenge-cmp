plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.push"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // FcmTokenProviderAndroid 가 FirebaseMessaging 을 직접 호출한다.
            // KMP KotlinDependencyHandler 에는 platform() 이 없어 project.dependencies.platform 을 쓴다.
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
        }
        commonMain.dependencies {
            // platformPushModule 이 Koin Module 을 수동 구성한다.
            implementation(libs.koin.core)
            // FcmTokenProviderAndroid 의 Logger
            implementation(projects.core.utils)
            implementation(libs.kotlinx.coroutines.core)
        }
        iosMain.dependencies {

        }
    }
}

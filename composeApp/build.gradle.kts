import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
    alias(libs.plugins.challengeKspKoin)
    alias(libs.plugins.buildkonfig)
}

// local.properties 의 kakao_native_app_key 를 두 곳에 주입:
//   1. Android manifestPlaceholder 의 ${kakaoNativeAppKey} → AuthCodeHandlerActivity scheme
//   2. BuildKonfig.KAKAO_NATIVE_APP_KEY → ChallengeApplication.onCreate 의 KakaoSdk.init
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val kakaoNativeAppKey: String =
    localProperties["kakao_native_app_key"]?.toString()?.trim('"').orEmpty()

kotlin {
    targets
        .filterIsInstance<KotlinNativeTarget>()
        .forEach { target ->
            target.binaries {
                framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }
        }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            // KakaoSdk.init 호출용. (feature:login 의 v2-user 가 transitively 가져오지만
            // composeApp 의 ChallengeApplication 에서 직접 import 하므로 명시적으로 추가)
            implementation(libs.kakao.sdk.user)
        }

        commonMain.dependencies {
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.annotations)

            implementation(projects.data.repositoryImpl)

            implementation(projects.feature.main)
            implementation(projects.feature.home)
            implementation(projects.feature.login)
            implementation(projects.feature.splash)
            implementation(projects.feature.ex1)
            implementation(projects.feature.ex2)
            implementation(projects.feature.ex3)
        }
    }
}

composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

buildkonfig {
    packageName = "com.lwg.challenge"
    defaultConfigs {
        buildConfigField(STRING, "KAKAO_NATIVE_APP_KEY", kakaoNativeAppKey)
    }
}

android {
    namespace = "com.lwg.challenge"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lwg.challenge"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKey
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}


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
    alias(libs.plugins.googleServices)
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
            // firebase-messaging 은 Android 전용 아티팩트라 androidMain 에만 둔다.
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)

            // ChallengeFirebaseMessagingService 전용.
            // NotificationCompat / NotificationChannelCompat
            implementation(libs.androidx.core.ktx)
            // KoinComponent / by inject() — Activity·ViewModel 밖(Service)에서 주입받는 유일한 지점
            implementation(libs.koin.core)
            // commonMain 의 projects.data.repositoryImpl 은 domain 모듈을 implementation 으로만
            // 물고 있어 전이되지 않는다. Service 가 UseCase/Repository/AuthTokens 를 직접 참조하므로 명시.
            implementation(projects.domain.usecase)
            implementation(projects.domain.repository)
            implementation(projects.domain.model)
            // Logger
            implementation(projects.core.utils)
            // Service 가 자체 CoroutineScope 를 들고 Flow 를 수집한다
            implementation(libs.kotlinx.coroutines.core)
        }

        commonMain.dependencies {
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.annotations)

            implementation(projects.data.repositoryImpl)

            // 챌린지 인증 사진이 이 앱의 첫 원격 이미지다. 네트워크 페처를 Ktor 구현으로 두는 이유는
            // App.kt 가 Koin 의 인증 HttpClient 를 그대로 페처에 물려주기 위해서다(Bearer + 401 갱신 재시도).
            implementation(libs.coil)
            implementation(libs.coil.network)
            // :remote:network 가 ktor-client-core 를 implementation 으로만 물어 전이되지 않는다.
            // App.kt 가 HttpClient 타입을 직접 참조하므로 명시 선언.
            implementation(libs.ktor.client.core)

            // App.kt 가 platformPushModule 을 직접 참조한다. :feature:main 이 :core:push 를
            // implementation 으로 물고 있어 전이되지 않으므로 명시 선언.
            implementation(projects.core.push)
            // App.kt 가 platformPermissionModule 을 직접 참조한다.
            implementation(projects.core.permission)

            implementation(projects.feature.main)
            implementation(projects.feature.home)
            implementation(projects.feature.login)
            implementation(projects.feature.splash)
            implementation(projects.feature.friends.list)
            implementation(projects.feature.friends.search)
            implementation(projects.feature.challenge.create)
            implementation(projects.feature.challenge.oath)
            implementation(projects.feature.challenge.detail)
            implementation(projects.feature.challenge.verify)
            implementation(projects.feature.ranking)
            implementation(projects.feature.mypage.mypage)
            implementation(projects.feature.mypage.archive)
            implementation(projects.feature.notification)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
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


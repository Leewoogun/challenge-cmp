import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
    alias(libs.plugins.challengeKspKoin)
}

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
        }

        commonMain.dependencies {
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.annotations)

            implementation(projects.data.repositoryImpl)

            implementation(projects.feature.main)
            implementation(projects.feature.home)
            implementation(projects.feature.ex1)
            implementation(projects.feature.ex2)
            implementation(projects.feature.ex3)
        }
    }
}

composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
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


import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.roborazzi.gradle.plugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    implementation(libs.room.runtime)
}

gradlePlugin {
    plugins {
        register("kmpIos") {
            id = "challenge.kmp.ios"
            implementationClass = "com.lwg.challenge.primitive.KotlinMultiPlatformiOSPlugin"
        }
        register("kmpAndroid") {
            id = "challenge.kmp.android"
            implementationClass = "com.lwg.challenge.primitive.KotlinMultiPlatformAndroidPlugin"
        }
        register("kmpPrimitive") {
            id = "challenge.kmp"
            implementationClass = "com.lwg.challenge.primitive.KotlinMultiPlatformPlugin"
        }
        register("detekt") {
            id = "challenge.verify.detekt"
            implementationClass = "com.lwg.challenge.primitive.DetektPlugin"
        }
        register("kspKoin") {
            id = "challenge.ksp.koin"
            implementationClass = "com.lwg.challenge.primitive.KspKoinPlugin"
        }
        register("kmpConvention") {
            id = "challenge.kotlin.multiplatform"
            implementationClass = "com.lwg.challenge.convention.KotlinMultiPlatformConventionPlugin"
        }
        register("cmpConvention") {
            id = "challenge.compose.multiplatform"
            implementationClass = "com.lwg.challenge.convention.ComposeMultiPlatformConventionPlugin"
        }
        register("kmpPureConvention") {
            id = "challenge.kotlin.multiplatform.pure"
            implementationClass = "com.lwg.challenge.convention.KotlinMultiPlatformPurePlugin"
        }
        register("challengeFeature") {
            id = "challenge.feature"
            implementationClass = "com.lwg.challenge.convention.ChallengeFeaturePlugin"
        }
    }
}
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
    alias(libs.plugins.buildkonfig)
}

android {
    namespace = "com.lwg.utils"

    buildFeatures {
        buildConfig = true
    }
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

// 키가 없으면 빈 값 → Constants 가 debug 빌드에서도 운영 서버를 쓴다.
fun devBaseUrl(key: String): String = localProperties[key]?.toString()?.trim('"').orEmpty()

buildkonfig {
    packageName = "com.lwg.challenge.utils"

    defaultConfigs {
        buildConfigField(STRING, "DEV_BASE_URL", devBaseUrl("challenge_api_base_url_android"))
    }
    targetConfigs {
        listOf("iosArm64", "iosSimulatorArm64", "iosX64").forEach { target ->
            create(target) {
                buildConfigField(STRING, "DEV_BASE_URL", devBaseUrl("challenge_api_base_url_ios"))
            }
        }
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {

        }
        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            // 서버 시간 규약(KST, `yyyy-MM-dd HH:mm:ss`)의 LocalDateTime 파싱과 타임존 산술에 사용
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {

        }
    }
}

plugins {
    alias(libs.plugins.challengeKotlinMultiplatformPure)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DataSource 시그니처가 도메인 모델을 그대로 노출하므로 implementation 이 아니라 api.
            api(projects.domain.model)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

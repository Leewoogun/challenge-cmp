plugins {
    alias(libs.plugins.challengeKotlinMultiplatformPure)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 시간 필드가 공개 타입(LocalDateTime)으로 노출되므로 implementation 이 아니라 api.
            api(libs.kotlinx.datetime)
        }
    }
}

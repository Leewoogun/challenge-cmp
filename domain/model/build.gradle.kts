plugins {
    alias(libs.plugins.challengeKotlinMultiplatformPure)
}

// 시각 타입은 Kotlin 2.2 부터 stdlib 의 `kotlin.time.Instant` 사용 (ExperimentalTime).
// 추가 의존성 불필요.

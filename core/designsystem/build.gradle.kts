plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.designsystem"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            // ChallengeCard / HomeEmptyState 가 lucide → Material 매핑(Flame, Clock, Schedule, Add, PersonAdd,
            // SportsKabaddi, CheckCircle, Cancel) 을 사용.
            implementation(compose.materialIconsExtended)

            // ChallengeCard.deadline = Instant, StatsBar/HomeEmptyState 내부에서 utils 헬퍼 사용.
            // :core:utils 는 :core:designsystem 미의존(역방향)이므로 안전하게 단방향.
            implementation(projects.core.utils)
        }
    }
}
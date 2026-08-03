plugins {
    alias(libs.plugins.challengeFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)

            implementation(projects.feature.home)
            implementation(projects.feature.login)
            implementation(projects.feature.splash)
            implementation(projects.feature.friends.list)
            implementation(projects.feature.friends.search)
            implementation(projects.feature.challenge.create)
            implementation(projects.feature.challenge.oath)
            implementation(projects.feature.challenge.detail)
            implementation(projects.feature.ranking)
            implementation(projects.feature.mypage)
        }
    }
}

android {
    namespace = "com.lwg.challenge.feature.main"
}

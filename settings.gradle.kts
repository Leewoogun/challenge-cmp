rootProject.name = "Challenge"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // Kakao SDK (Android) — `com.kakao.sdk:*` 이 Maven Central 에 미러링되어 있지 않아 공식 저장소 추가.
        maven("https://devrepo.kakao.com/nexus/content/groups/public/") {
            content { includeGroup("com.kakao.sdk") }
        }
    }
}

include(":composeApp")

// Domain
include(":domain:model")
include(":domain:repository")
include(":domain:usecase")

// Remote
include(":remote:network")
include(":remote:api")
include(":remote:datasource")
include(":remote:model")
include(":remote:mapper")

// Data
include(":data:datasource")
include(":data:repositoryImpl")

// Local
include(":local:datastore")
include(":local:database")

// Core
include(":core:designsystem")
include(":core:ui")
include(":core:utils")
include(":core:navigation")
include(":core:push")
include(":core:permission")

// Feature
include(":feature:main")
include(":feature:home")
include(":feature:login")
include(":feature:splash")
include(":feature:friends:list")
include(":feature:friends:search")
include(":feature:challenge:create")
include(":feature:challenge:oath")
include(":feature:challenge:detail")
include(":feature:ranking")
include(":feature:mypage")

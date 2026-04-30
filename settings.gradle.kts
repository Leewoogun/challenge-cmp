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
include(":remote:model")
include(":remote:mapper")

// Data
include(":data:repositoryImpl")

// Local
include(":local:datastore")
include(":local:database")

// Core
include(":core:designsystem")
include(":core:utils")
include(":core:navigation")

// Feature
include(":feature:main")
include(":feature:home")
include(":feature:login")
include(":feature:splash")
include(":feature:ex1")
include(":feature:ex2")
include(":feature:ex3")

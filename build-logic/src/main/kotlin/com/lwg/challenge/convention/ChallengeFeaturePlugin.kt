package com.lwg.challenge.convention

import com.lwg.challenge.library
import com.lwg.challenge.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ChallengeFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("androidLibrary").get().get().pluginId)
        }

        apply(plugin = "challenge.kotlin.multiplatform")
        apply(plugin = "challenge.compose.multiplatform")
        apply(plugin = "challenge.ksp.koin")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain {
                    dependencies {
                        implementation(project(":core:designsystem"))
                        implementation(project(":core:navigation"))
                        implementation(project(":domain:model"))
                        implementation(project(":domain:repository"))
                        implementation(project(":domain:usecase"))
                        implementation(project(":core:utils"))
                        implementation(libs.library("androidx-navigation3-ui"))
                        implementation(libs.library("androidx-lifecycle-viewmodel-navigation3"))
                        implementation(libs.library("androidx-lifecycle-runtime-compose"))
                        implementation(libs.library("koin-compose-viewmodel-navigation"))
                        implementation(libs.library("koin-annotations"))
                    }
                }
            }
        }
    }
}

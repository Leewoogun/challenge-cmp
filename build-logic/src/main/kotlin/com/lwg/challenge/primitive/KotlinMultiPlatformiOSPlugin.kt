package com.lwg.challenge.primitive

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class KotlinMultiPlatformiOSPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        extensions.configure<KotlinMultiplatformExtension> {
            // iOS Device (실제 기기)
            iosArm64()

            // iOS Simulator (M1+ Mac)
            iosSimulatorArm64()

            targets.withType<KotlinNativeTarget> {
                compilations["main"].compileTaskProvider.configure {
                    compilerOptions {
                        freeCompilerArgs.add("-Xexport-kdoc")
                    }
                }
            }
        }
    }
}

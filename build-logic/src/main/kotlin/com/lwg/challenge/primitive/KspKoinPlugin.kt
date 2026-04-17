package com.lwg.challenge.primitive

import com.lwg.challenge.kotlin
import com.lwg.challenge.library
import com.lwg.challenge.libs
import com.lwg.challenge.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class KspKoinPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.plugin("ksp").pluginId)
        }

        // KSP 생성 코드를 commonMain에 포함
        kotlin {
            sourceSets.commonMain {
                kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            }
        }

        val koinKspCompiler = libs.library("koin-ksp-compiler")
        val kspTargets = listOf(
            "kspCommonMainMetadata",
            "kspAndroid",
            "kspIosArm64",
            "kspIosSimulatorArm64",
        )

        kspTargets.forEach { target ->
            dependencies.add(target, koinKspCompiler)
        }

        // KMP + KSP 태스크 의존성 workaround
        // https://github.com/google/ksp/issues/567
        tasks.configureEach {
            if (name != "kspCommonMainKotlinMetadata" &&
                (name.startsWith("ksp") || name.startsWith("compile"))
            ) {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}

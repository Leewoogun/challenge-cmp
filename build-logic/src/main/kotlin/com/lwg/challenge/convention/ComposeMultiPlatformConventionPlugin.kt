package com.lwg.challenge.convention

import com.lwg.challenge.libs
import com.lwg.challenge.primitive.composeCompiler
import com.lwg.challenge.primitive.composeMultiplatformDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeMultiPlatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("composeCompiler").get().get().pluginId)
        }

        composeCompiler {
            stabilityConfigurationFiles.add(
                rootProject.layout.projectDirectory.file("compose_compiler_config.conf"),
            )
        }

        composeMultiplatformDependencies()
    }
}

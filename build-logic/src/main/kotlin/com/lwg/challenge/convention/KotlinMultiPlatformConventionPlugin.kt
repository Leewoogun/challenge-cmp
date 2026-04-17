package com.lwg.challenge.convention

import com.lwg.challenge.libs
import com.lwg.challenge.primitive.DetektPlugin
import com.lwg.challenge.primitive.KotlinMultiPlatformAndroidPlugin
import com.lwg.challenge.primitive.KotlinMultiPlatformPlugin
import com.lwg.challenge.primitive.KotlinMultiPlatformiOSPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KotlinMultiPlatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
        }

        apply<KotlinMultiPlatformPlugin>()
        apply<KotlinMultiPlatformAndroidPlugin>()
        apply<KotlinMultiPlatformiOSPlugin>()
        apply<DetektPlugin>()
    }
}
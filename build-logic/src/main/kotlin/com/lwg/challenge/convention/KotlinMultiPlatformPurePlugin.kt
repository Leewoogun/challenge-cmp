package com.lwg.challenge.convention

import com.lwg.challenge.primitive.DetektPlugin
import com.lwg.challenge.primitive.KotlinMultiPlatformPlugin
import com.lwg.challenge.primitive.KotlinMultiPlatformiOSPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * 순수 Kotlin KMP 모듈용 컨벤션 플러그인
 *
 * Android 의존성 없이 JVM + iOS 타겟만 구성합니다.
 * Domain 레이어처럼 플랫폼 독립적인 순수 Kotlin 모듈에 사용합니다.
 */
class KotlinMultiPlatformPurePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply<KotlinMultiPlatformPlugin>()
        apply<KotlinMultiPlatformiOSPlugin>()
        apply<DetektPlugin>()

        extensions.configure<KotlinMultiplatformExtension> {
            jvm()
        }
    }
}

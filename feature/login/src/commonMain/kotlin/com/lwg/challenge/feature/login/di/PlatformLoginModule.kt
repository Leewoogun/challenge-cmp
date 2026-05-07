package com.lwg.challenge.feature.login.di

import org.koin.core.module.Module

/**
 * 플랫폼별 [com.lwg.challenge.feature.login.kakao.KakaoAuthClient] 바인딩 모듈.
 *
 * Feature 스킬 SKILL.md 가이드: actual class 에 `@Single` 을 붙이지 않고 expect/actual val Module 로
 * 수동 등록한다 (KSP metadata pass 가 platform-specific @Single 을 누락하기 때문).
 */
expect val platformLoginModule: Module

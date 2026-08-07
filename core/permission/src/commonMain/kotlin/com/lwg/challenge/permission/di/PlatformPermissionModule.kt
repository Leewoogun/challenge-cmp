package com.lwg.challenge.permission.di

import org.koin.core.module.Module

/**
 * 플랫폼별 [com.lwg.challenge.permission.PermissionManager] 바인딩 모듈.
 *
 * moko 의 `PermissionsController` 생성 인자가 플랫폼마다 달라(Android 는 Context 필요) 여기서 갈린다.
 * `platformPushModule` 과 같은 이유·같은 방식이다 — KSP metadata pass 가 platform-specific `@Single`
 * 을 누락하므로 expect/actual `val Module` 로 수동 등록한다.
 */
expect val platformPermissionModule: Module

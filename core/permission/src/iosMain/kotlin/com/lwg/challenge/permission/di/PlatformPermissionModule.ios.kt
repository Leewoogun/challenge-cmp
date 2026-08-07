package com.lwg.challenge.permission.di

import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.permission.PermissionManagerImpl
import dev.icerock.moko.permissions.ios.PermissionsController
import org.koin.core.module.Module
import org.koin.dsl.module

// iOS 에서 dev.icerock.moko.permissions.PermissionsController 는 protocol 의 typealias 라
// 생성자가 없다. 구현체는 .ios 패키지의 동명 클래스다.
actual val platformPermissionModule: Module = module {
    single<PermissionManager> { PermissionManagerImpl(PermissionsController()) }
}

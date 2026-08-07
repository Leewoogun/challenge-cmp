package com.lwg.challenge.permission.di

import android.content.Context
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.permission.PermissionManagerImpl
import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPermissionModule: Module = module {
    // Activity 가 아니라 applicationContext 다 — 컨트롤러는 앱 수명 내내 살아 있고,
    // Activity 는 PermissionManager.Bind() 가 매번 다시 붙인다.
    single<PermissionManager> {
        PermissionManagerImpl(PermissionsController(applicationContext = get<Context>()))
    }
}

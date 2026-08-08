package com.lwg.challenge.permission.di

import android.content.Context
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.permission.PermissionManagerImpl
import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPermissionModule: Module = module {
    single<PermissionManager> {
        PermissionManagerImpl(PermissionsController(applicationContext = get<Context>()))
    }
}

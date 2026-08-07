package com.lwg.challenge.push.di

import com.lwg.challenge.push.FcmTokenProvider
import com.lwg.challenge.push.FcmTokenProviderIos
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPushModule: Module = module {
    single<FcmTokenProvider> { FcmTokenProviderIos() }
}

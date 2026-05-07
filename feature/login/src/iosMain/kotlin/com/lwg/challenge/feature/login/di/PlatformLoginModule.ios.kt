package com.lwg.challenge.feature.login.di

import com.lwg.challenge.feature.login.kakao.KakaoAuthClient
import com.lwg.challenge.feature.login.kakao.KakaoAuthClientIos
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLoginModule: Module = module {
    single<KakaoAuthClient> { KakaoAuthClientIos() }
}

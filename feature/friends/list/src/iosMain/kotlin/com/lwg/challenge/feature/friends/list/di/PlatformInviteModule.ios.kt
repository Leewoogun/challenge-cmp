package com.lwg.challenge.feature.friends.list.di

import com.lwg.challenge.feature.friends.list.kakao.IosKakaoInviter
import com.lwg.challenge.feature.friends.list.kakao.KakaoInviter
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformInviteModule: Module = module {
    single<KakaoInviter> { IosKakaoInviter() }
}

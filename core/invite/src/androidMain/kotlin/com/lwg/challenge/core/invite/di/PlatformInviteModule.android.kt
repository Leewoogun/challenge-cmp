package com.lwg.challenge.core.invite.di

import com.lwg.challenge.core.invite.AndroidKakaoInviter
import com.lwg.challenge.core.invite.KakaoInviter
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformInviteModule: Module = module {
    single<KakaoInviter> { AndroidKakaoInviter(context = get()) }
}

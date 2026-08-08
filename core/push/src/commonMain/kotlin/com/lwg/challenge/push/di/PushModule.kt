package com.lwg.challenge.push.di

import com.lwg.challenge.push.PushEventBus
import org.koin.core.module.Module
import org.koin.dsl.module

/** `AppModule` 의 `@ComponentScan` 이 다른 모듈까지 닿지 않아 수동 등록한다. */
val pushModule: Module = module {
    single { PushEventBus() }
}

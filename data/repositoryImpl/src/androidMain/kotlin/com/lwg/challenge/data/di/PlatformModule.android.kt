package com.lwg.challenge.data.di

import com.lwg.challenge.data.storage.SecureTokenStorage
import com.lwg.challenge.data.storage.SecureTokenStorageImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    single<SecureTokenStorage> { SecureTokenStorageImpl(get()) }
}

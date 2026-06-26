package com.lwg.challenge.data.di

import com.lwg.challenge.local.datastore.di.DataStoreModule
import com.lwg.challenge.remote.api.di.ApiModule
import com.lwg.challenge.remote.network.di.NetworkModule
import org.koin.core.annotation.Module

/**
 * Data 레이어의 모든 Koin 모듈을 통합
 */
@Module(
    includes = [
        NetworkModule::class,
        ApiModule::class,
        RepositoryModule::class,
        UseCaseModule::class,
        DataStoreModule::class,
    ],
)
class DataModule

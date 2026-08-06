package com.lwg.challenge.remote.datasource.di

import com.lwg.challenge.remote.api.di.ApiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/** Ktorfit Api 를 쓰는 유일한 레이어라 [ApiModule] 도 여기서 끌어온다. */
@Module(includes = [ApiModule::class])
@ComponentScan("com.lwg.challenge.remote.datasource")
class RemoteDataSourceModule

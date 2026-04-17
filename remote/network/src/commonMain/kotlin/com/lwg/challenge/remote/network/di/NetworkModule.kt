package com.lwg.challenge.remote.network.di

import org.koin.core.annotation.Module

/**
 * Network 레이어의 Koin 모듈을 통합
 *
 * KtorfitModule을 includes로 포함합니다.
 */
@Module(includes = [KtorfitModule::class])
class NetworkModule

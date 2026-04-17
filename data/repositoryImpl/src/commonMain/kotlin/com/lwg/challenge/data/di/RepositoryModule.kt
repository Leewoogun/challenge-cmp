package com.lwg.challenge.data.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Repository 계층을 제공하는 Koin 모듈
 *
 * @ComponentScan으로 data.repository 패키지 내의 @Single 어노테이션을 자동 스캔합니다.
 */
@Module
@ComponentScan("com.lwg.challenge.data.repository")
class RepositoryModule

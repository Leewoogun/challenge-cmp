package com.lwg.challenge.data.di

import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.domain.usecase.GetStoredTokensUseCase
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.domain.usecase.RefreshAccessTokenUseCase
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

/**
 * Domain UseCase 를 Koin 에 등록.
 *
 * UseCase 는 pure Kotlin (`:domain:usecase`) 이므로 Koin annotation 을 붙일 수 없어,
 * data 레이어에서 팩토리로 등록한다.
 */
@Module
class UseCaseModule {

    @Factory
    fun provideLoginWithKakaoUseCase(
        loginRepository: LoginRepository,
    ): LoginWithKakaoUseCase = LoginWithKakaoUseCase(loginRepository)

    @Factory
    fun provideRefreshAccessTokenUseCase(
        loginRepository: LoginRepository,
    ): RefreshAccessTokenUseCase = RefreshAccessTokenUseCase(loginRepository)

    @Factory
    fun provideGetStoredTokensUseCase(
        loginRepository: LoginRepository,
    ): GetStoredTokensUseCase = GetStoredTokensUseCase(loginRepository)
}

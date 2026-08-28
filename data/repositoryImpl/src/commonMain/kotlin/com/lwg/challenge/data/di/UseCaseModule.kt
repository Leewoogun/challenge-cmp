package com.lwg.challenge.data.di

import com.lwg.challenge.domain.repository.ActiveChallengeRepository
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.domain.repository.RecordRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.domain.repository.WithdrawRepository
import com.lwg.challenge.domain.usecase.CreateChallengeUseCase
import com.lwg.challenge.domain.usecase.GetChallengeHistoriesUseCase
import com.lwg.challenge.domain.usecase.GetFriendsDataUseCase
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.domain.usecase.GetMyPageDataUseCase
import com.lwg.challenge.domain.usecase.GetStoredTokensUseCase
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.domain.usecase.LoginWithTestAccountUseCase
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.domain.usecase.WithdrawUseCase
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
    fun provideLoginWithTestAccountUseCase(
        logoutUseCase: LogoutUseCase,
        loginRepository: LoginRepository,
    ): LoginWithTestAccountUseCase = LoginWithTestAccountUseCase(logoutUseCase, loginRepository)

    @Factory
    fun provideGetStoredTokensUseCase(
        loginRepository: LoginRepository,
    ): GetStoredTokensUseCase = GetStoredTokensUseCase(loginRepository)

    @Factory
    fun provideGetHomeDataUseCase(
        recordRepository: RecordRepository,
        activeChallengeRepository: ActiveChallengeRepository,
        userInfoRepository: UserInfoRepository,
        challengeRepository: ChallengeRepository,
    ): GetHomeDataUseCase = GetHomeDataUseCase(
        recordRepository,
        activeChallengeRepository,
        userInfoRepository,
        challengeRepository,
    )

    @Factory
    fun provideGetMyPageDataUseCase(
        recordRepository: RecordRepository,
        userInfoRepository: UserInfoRepository,
    ): GetMyPageDataUseCase = GetMyPageDataUseCase(
        recordRepository,
        userInfoRepository,
    )

    @Factory
    fun provideGetFriendsDataUseCase(
        friendsRepository: FriendsRepository,
    ): GetFriendsDataUseCase = GetFriendsDataUseCase(friendsRepository)

    @Factory
    fun provideGetChallengeHistoriesUseCase(
        challengeRepository: ChallengeRepository,
    ): GetChallengeHistoriesUseCase = GetChallengeHistoriesUseCase(challengeRepository)

    @Factory
    fun provideCreateChallengeUseCase(
        challengeRepository: ChallengeRepository,
    ): CreateChallengeUseCase = CreateChallengeUseCase(challengeRepository)

    @Factory
    fun provideLogoutUseCase(
        loginRepository: LoginRepository,
        userInfoRepository: UserInfoRepository,
    ): LogoutUseCase = LogoutUseCase(loginRepository, userInfoRepository)

    @Factory
    fun provideWithdrawUseCase(
        withdrawRepository: WithdrawRepository,
        logoutUseCase: LogoutUseCase,
    ): WithdrawUseCase = WithdrawUseCase(withdrawRepository, logoutUseCase)
}

package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.domain.repository.UserInfoRepository

class LogoutUseCase(
    private val loginRepository: LoginRepository,
    private val userInfoRepository: UserInfoRepository,
) {
    suspend operator fun invoke() {
        loginRepository.clearTokens()
        userInfoRepository.clearUserInfoCache()
    }
}

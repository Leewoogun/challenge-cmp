package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.LogoutReason
import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.domain.repository.UserInfoRepository

class LogoutUseCase(
    private val loginRepository: LoginRepository,
    private val userInfoRepository: UserInfoRepository,
) {
    suspend operator fun invoke(reason: LogoutReason) {
        // 나머지 둘은 토큰이 이미 없어 401 → 갱신 실패 → 이 흐름에 재진입한다.
        if (reason == LogoutReason.USER) {
            loginRepository.logout()
        }

        loginRepository.clearTokens()
        userInfoRepository.clearUserInfoCache()
    }
}

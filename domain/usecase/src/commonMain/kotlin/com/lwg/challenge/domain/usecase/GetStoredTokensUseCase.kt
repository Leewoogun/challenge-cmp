package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.repository.LoginRepository

/**
 * Secure storage 에서 현재 저장된 토큰을 조회.
 *
 * Splash 화면에서 저장된 refresh 존재 여부 판단 시 사용.
 */
class GetStoredTokensUseCase(
    private val loginRepository: LoginRepository,
) {

    suspend operator fun invoke(): AuthTokens = loginRepository.getStoredTokens()
}

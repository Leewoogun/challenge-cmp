package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.repository.LoginRepository

/**
 * 저장된 refresh token 으로 access token 재발급.
 *
 * 성공 시 갱신된 [AuthTokens] 반환, 실패(code=401 등) 시 null → 로그인 화면으로.
 */
class RefreshAccessTokenUseCase(
    private val loginRepository: LoginRepository,
) {

    suspend operator fun invoke(
        onError: (code: Int, message: String) -> Unit,
    ): AuthTokens? = loginRepository.refreshAccessToken(onError = onError)
}

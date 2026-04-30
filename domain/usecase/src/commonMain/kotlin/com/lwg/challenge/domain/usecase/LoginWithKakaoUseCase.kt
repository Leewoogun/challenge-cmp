package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository

/**
 * Kakao access token 을 서버에 보내 로그인/가입 처리.
 *
 * 성공 시 [LoginResult] (tokens + user profile) 반환, 실패 시 null 과 onError 콜백.
 */
class LoginWithKakaoUseCase(
    private val loginRepository: LoginRepository,
) {

    suspend operator fun invoke(
        kakaoAccessToken: String,
        onError: (code: Int, message: String) -> Unit,
    ): LoginResult? = loginRepository.loginWithKakao(
        kakaoAccessToken = kakaoAccessToken,
        onError = onError,
    )
}

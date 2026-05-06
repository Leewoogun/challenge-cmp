package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow

class LoginWithKakaoUseCase(
    private val loginRepository: LoginRepository,
) {

    operator fun invoke(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = loginRepository.loginWithKakao(
        kakaoAccessToken = kakaoAccessToken,
        onError = onError,
    )
}

package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow

class RefreshAccessTokenUseCase(
    private val loginRepository: LoginRepository,
) {

    operator fun invoke(
        onError: (String) -> Unit,
    ): Flow<AuthTokens> = loginRepository.refreshAccessToken(onError = onError)
}

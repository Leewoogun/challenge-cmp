package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult>

    suspend fun getStoredTokens(): AuthTokens

    suspend fun clearTokens()
}

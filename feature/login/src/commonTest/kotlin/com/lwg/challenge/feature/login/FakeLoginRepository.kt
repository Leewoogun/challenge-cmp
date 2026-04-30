package com.lwg.challenge.feature.login

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.model.UserProfile
import com.lwg.challenge.domain.repository.LoginRepository

/**
 * 테스트용 Fake LoginRepository. [loginWithKakao] 응답을 예약해두고 반환.
 */
class FakeLoginRepository : LoginRepository {

    var loginResponse: LoginResponse = LoginResponse.Success(
        result = LoginResult(
            tokens = AuthTokens(accessToken = "acc", refreshToken = "ref"),
            userProfile = UserProfile(userId = 10L, isNewUser = false),
        ),
    )

    var refreshResponse: RefreshResponse = RefreshResponse.Success(
        tokens = AuthTokens(accessToken = "acc", refreshToken = "ref"),
    )

    var storedTokens: AuthTokens = AuthTokens.Empty

    override suspend fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (code: Int, message: String) -> Unit,
    ): LoginResult? = when (val r = loginResponse) {
        is LoginResponse.Success -> r.result
        is LoginResponse.Error -> {
            onError(r.code, r.message)
            null
        }
    }

    override suspend fun refreshAccessToken(
        onError: (code: Int, message: String) -> Unit,
    ): AuthTokens? = when (val r = refreshResponse) {
        is RefreshResponse.Success -> r.tokens
        is RefreshResponse.Error -> {
            onError(r.code, r.message)
            null
        }
        RefreshResponse.Unauthorized -> null
    }

    override suspend fun getStoredTokens(): AuthTokens = storedTokens

    override suspend fun clearTokens() {
        storedTokens = AuthTokens.Empty
    }

    sealed interface LoginResponse {
        data class Success(val result: LoginResult) : LoginResponse
        data class Error(val code: Int, val message: String) : LoginResponse
    }

    sealed interface RefreshResponse {
        data class Success(val tokens: AuthTokens) : RefreshResponse
        data class Error(val code: Int, val message: String) : RefreshResponse
        data object Unauthorized : RefreshResponse
    }
}

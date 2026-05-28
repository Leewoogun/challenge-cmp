package com.lwg.challenge.feature.login

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.model.UserProfile
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

    var storedTokens: AuthTokens = AuthTokens.Empty

    override fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = flow {
        when (val r = loginResponse) {
            is LoginResponse.Success -> emit(r.result)
            is LoginResponse.Error -> onError(r.message)
        }
    }

    override suspend fun getStoredTokens(): AuthTokens = storedTokens

    override suspend fun clearTokens() {
        storedTokens = AuthTokens.Empty
    }

    sealed interface LoginResponse {
        data class Success(val result: LoginResult) : LoginResponse
        data class Error(val message: String) : LoginResponse
    }
}

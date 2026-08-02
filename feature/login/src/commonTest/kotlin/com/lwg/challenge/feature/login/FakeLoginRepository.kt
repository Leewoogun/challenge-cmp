package com.lwg.challenge.feature.login

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 테스트용 Fake LoginRepository. [loginWithKakao] / [loginWithTestAccount] 응답을 예약해두고 반환.
 */
class FakeLoginRepository : LoginRepository {

    var loginResponse: LoginResponse = LoginResponse.Success(
        result = LoginResult(
            tokens = AuthTokens(accessToken = "acc", refreshToken = "ref"),
            userId = 10L,
            isNewUser = false,
        ),
    )

    var testLoginResponse: TestLoginResponse = TestLoginResponse.Success(
        result = LoginResult(
            tokens = AuthTokens(accessToken = "acc", refreshToken = "ref"),
            userId = 10L,
            isNewUser = false,
        ),
    )

    /** 설정하면 [loginWithTestAccount] 가 완료될 때까지 응답을 보류한다 — 중복 진입 테스트용. */
    var testLoginGate: CompletableDeferred<Unit>? = null

    var lastTestUserNo: Int? = null
        private set

    /** LogoutUseCase(clearTokens) → loginWithTestAccount 호출 순서 검증용. */
    val callLog = mutableListOf<String>()

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

    override fun loginWithTestAccount(
        testUserNo: Int,
        onError: (String) -> Unit,
        onUnsupported: () -> Unit,
    ): Flow<LoginResult> = flow {
        callLog += "loginWithTestAccount"
        lastTestUserNo = testUserNo
        testLoginGate?.await()
        when (val r = testLoginResponse) {
            is TestLoginResponse.Success -> emit(r.result)
            is TestLoginResponse.Error -> onError(r.message)
            is TestLoginResponse.Unsupported -> onUnsupported()
        }
    }

    override suspend fun getStoredTokens(): AuthTokens = storedTokens

    override suspend fun clearTokens() {
        callLog += "clearTokens"
        storedTokens = AuthTokens.Empty
    }

    sealed interface LoginResponse {
        data class Success(val result: LoginResult) : LoginResponse
        data class Error(val message: String) : LoginResponse
    }

    sealed interface TestLoginResponse {
        data class Success(val result: LoginResult) : TestLoginResponse
        data class Error(val message: String) : TestLoginResponse
        data object Unsupported : TestLoginResponse
    }
}

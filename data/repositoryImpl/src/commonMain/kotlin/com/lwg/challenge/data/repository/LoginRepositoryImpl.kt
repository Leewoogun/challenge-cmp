package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.local.TokenLocalDataSource
import com.lwg.challenge.data.datasource.remote.LoginRemoteDataSource
import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.remote.network.auth.AuthTokenCacheInvalidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single

@Single(binds = [LoginRepository::class])
class LoginRepositoryImpl(
    private val loginRemoteDataSource: LoginRemoteDataSource,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val authTokenCacheInvalidator: AuthTokenCacheInvalidator,
) : LoginRepository {

    override fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = loginRemoteDataSource
        .loginWithKakao(kakaoAccessToken, onError)
        .onEach { persistSession(it) }

    override fun loginWithTestAccount(
        testUserNo: Int,
        onError: (String) -> Unit,
        onUnsupported: () -> Unit,
    ): Flow<LoginResult> = loginRemoteDataSource
        .loginWithTestAccount(testUserNo, onError, onUnsupported)
        .onEach { persistSession(it) }

    // 카카오/테스트 로그인의 성공 후처리는 반드시 같은 경로를 탄다 (dev-test-login 계약 —
    // 테스트 로그인으로 검증한 것이 실제 로그인을 보증해야 한다).
    // onEach 라서 downstream 이 결과를 받기 전에 저장이 끝난다 — 구독자가 곧바로 인증 요청을 걸어도 안전하다.
    private suspend fun persistSession(result: LoginResult) {
        tokenLocalDataSource.updateTokens(
            accessToken = result.tokens.accessToken,
            refreshToken = result.tokens.refreshToken,
        )
        // Ktor Auth 는 loadTokens 결과를 캐싱하므로, 비우지 않으면 다음 요청이 옛 토큰으로 나간다
        authTokenCacheInvalidator.invalidate()
    }

    override suspend fun getStoredTokens(): AuthTokens {
        val access = tokenLocalDataSource.getAccessToken()
        val refresh = tokenLocalDataSource.getRefreshToken()
        return if (access.isBlank() && refresh.isBlank()) {
            AuthTokens.Empty
        } else {
            AuthTokens(accessToken = access, refreshToken = refresh)
        }
    }

    override suspend fun clearTokens() {
        tokenLocalDataSource.clearTokens()
        authTokenCacheInvalidator.invalidate()
    }
}

package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.local.datastore.datasource.TokenLocalDataSource
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.mapper.toLoginResult
import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.LoginResponse
import com.lwg.challenge.remote.model.auth.TestLoginRequest
import com.lwg.challenge.remote.network.auth.AuthTokenCacheInvalidator
import com.lwg.challenge.remote.network.util.ApiResult
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [LoginRepository::class])
class LoginRepositoryImpl(
    private val loginApi: LoginApi,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val authTokenCacheInvalidator: AuthTokenCacheInvalidator,
) : LoginRepository {

    override fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = flow {
        loginApi.kakaoLogin(KakaoLoginRequest(kakaoAccessToken))
            .suspendOnSuccess { this@flow.handleLoginSuccess(response) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override fun loginWithTestAccount(
        testUserNo: Int,
        onError: (String) -> Unit,
        onUnsupported: () -> Unit,
    ): Flow<LoginResult> = flow {
        val result = loginApi.testLogin(TestLoginRequest(testUserNo))
        // 404(서버 설정 off → 라우팅 부재)와 401(서버 격리 설정 T-B1a/T-B1b 배포 전)은 둘 다
        // "이 서버는 테스트 로그인 미지원" 신호다. onError 로 보내지 않는 이유: HttpError.message 는
        // Ktor 가 채운 영문 status description("Not Found"/"Unauthorized")이라 스낵바에 그대로
        // 노출되는데, 서버가 통제할 수 없는 문자열이다.
        if (result is ApiResult.Failure.HttpError && (result.code == 404 || result.code == 401)) {
            onUnsupported()
            return@flow
        }
        result
            .suspendOnSuccess { this@flow.handleLoginSuccess(response) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    // 카카오/테스트 로그인의 성공 후처리는 반드시 같은 경로를 탄다 (dev-test-login 계약 —
    // 테스트 로그인으로 검증한 것이 실제 로그인을 보증해야 한다)
    private suspend fun FlowCollector<LoginResult>.handleLoginSuccess(response: LoginResponse) {
        val result = response.toLoginResult()
        tokenLocalDataSource.updateTokens(
            accessToken = result.tokens.accessToken,
            refreshToken = result.tokens.refreshToken,
        )
        // Ktor Auth 는 loadTokens 결과를 캐싱하므로, 비우지 않으면 다음 요청이 옛 토큰으로 나간다
        authTokenCacheInvalidator.invalidate()
        emit(result)
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

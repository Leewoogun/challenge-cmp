package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.LoginRemoteDataSource
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.mapper.toLoginResult
import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.TestLoginRequest
import com.lwg.challenge.remote.network.util.ApiResult
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [LoginRemoteDataSource::class])
class LoginRemoteDataSourceImpl(
    private val loginApi: LoginApi,
) : LoginRemoteDataSource {

    override fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = flow {
        loginApi.kakaoLogin(KakaoLoginRequest(kakaoAccessToken))
            .suspendOnSuccess { this@flow.emit(response.toLoginResult()) }
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
            .suspendOnSuccess { this@flow.emit(response.toLoginResult()) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    // 방출할 값도 UI 에 전할 에러도 없어 Flow 가 아닌 suspend 다. 실패는 기본 onError({}) 로 로그만 남긴다.
    override suspend fun logout() {
        loginApi.logout()
            .suspendOnFailureWithErrorHandling()
    }
}

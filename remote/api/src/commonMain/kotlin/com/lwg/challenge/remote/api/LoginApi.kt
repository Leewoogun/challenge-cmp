package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.LoginResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

/**
 * 인증 관련 API.
 *
 * - 카카오 로그인은 공개 엔드포인트 (Authorization header 불필요).
 * - 토큰 갱신(refresh)은 Ktor Auth 플러그인(KtorfitModule)이 직접 처리.
 * - 서버는 항상 HTTP 200 을 반환하며 body.code 로 성공/에러를 구분 (ADR-0002).
 */
interface LoginApi {

    @POST("api/v1/auth/kakao")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest,
    ): ApiResult<LoginResponse>
}

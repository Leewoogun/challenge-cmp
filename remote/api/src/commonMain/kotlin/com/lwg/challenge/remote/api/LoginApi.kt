package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.LoginResponse
import com.lwg.challenge.remote.model.auth.LogoutResponse
import com.lwg.challenge.remote.model.auth.TestLoginRequest
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.POST

/**
 * 로그인(auth) API.
 *
 * 로그인 계열([kakaoLogin]/[testLogin])만 공개 엔드포인트이고, [logout] 은 Authorization header 가
 * **필수** 다 — 서버가 어느 세션을 끊을지 토큰으로 판별한다.
 *
 * 서버는 항상 HTTP 200 을 반환하며 body.code 로 성공/에러를 구분 (ADR-0002).
 * 따라서 ApiResult.Success 라도 response.code != 200 일 수 있고, 이는 Repository 레이어에서 처리한다.
 */
interface LoginApi {

    @POST("api/v1/auth/kakao")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest,
    ): ApiResult<LoginResponse>

    /**
     * 테스트 계정으로 즉시 로그인 (debug 전용). 응답 shape 는 [kakaoLogin] 과 완전히 동일하다.
     *
     * 서버 설정이 꺼져 있으면 이 경로는 존재하지 않아 404 가 온다.
     */
    @POST("api/v1/auth/test-login")
    suspend fun testLogin(
        @Body request: TestLoginRequest,
    ): ApiResult<LoginResponse>

    /** DELETE /api/v1/auth/logout — 서버 세션(refresh hash·fcm token) 무효화. 멱등하다. */
    @DELETE("api/v1/auth/logout")
    suspend fun logout(): ApiResult<LogoutResponse>
}

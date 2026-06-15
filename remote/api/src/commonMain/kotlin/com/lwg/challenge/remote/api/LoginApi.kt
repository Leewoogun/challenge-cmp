package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.LoginResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

/**
 * 카카오 로그인 API. 공개 엔드포인트 (Authorization header 불필요).
 *
 * 서버는 항상 HTTP 200 을 반환하며 body.code 로 성공/에러를 구분 (ADR-0002).
 * 따라서 ApiResult.Success 라도 response.code != 200 일 수 있고, 이는 Repository 레이어에서 처리한다.
 */
interface LoginApi {

    @POST("api/v1/auth/kakao")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest,
    ): ApiResult<LoginResponse>
}

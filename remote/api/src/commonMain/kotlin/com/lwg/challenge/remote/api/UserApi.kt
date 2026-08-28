package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.user.FcmTokenRequest
import com.lwg.challenge.remote.model.user.FcmTokenResponse
import com.lwg.challenge.remote.model.user.UserInfoResponse
import com.lwg.challenge.remote.model.user.WithdrawResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT

interface UserApi {
    @GET("api/v1/users/me")
    suspend fun getMe(): ApiResult<UserInfoResponse>

    /**
     * 멱등하다. 모바일이 로그인 직후와 onNewToken 양쪽에서 부르므로 중복 호출이 정상 경로다.
     */
    @PUT("api/v1/users/me/fcm-token")
    suspend fun updateFcmToken(
        @Body body: FcmTokenRequest,
    ): ApiResult<FcmTokenResponse>

    /**
     * 회원탈퇴. body 없음. 이미 탈퇴한 계정으로 다시 불러도 200 이다.
     *
     * 로그아웃과 달리 실패가 실재한다 — 카카오 연결 해제가 실패하면 `code 701` 이 오고,
     * 그건 재시도로 풀리는 일시적 실패다.
     */
    @DELETE("api/v1/users/me")
    suspend fun withdraw(): ApiResult<WithdrawResponse>
}

package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.user.FcmTokenRequest
import com.lwg.challenge.remote.model.user.FcmTokenResponse
import com.lwg.challenge.remote.model.user.UserInfoResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
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
}

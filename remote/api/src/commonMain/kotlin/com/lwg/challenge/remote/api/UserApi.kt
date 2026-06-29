package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.user.UserInfoResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.GET

interface UserApi {
    @GET("api/v1/users/me")
    suspend fun getMe(): ApiResult<UserInfoResponse>
}

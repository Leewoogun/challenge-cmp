package com.lwg.challenge.remote.model.auth

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/** POST /api/v1/auth/kakao · POST /api/v1/auth/test-login 공통 응답. */
@Serializable
data class LoginResponse(
    val data: LoginData = LoginData(),
) : BaseResponse()

@Serializable
data class LoginData(
    val accessToken: String = "",
    val refreshToken: String = "",
    val userId: Long = 0L,
    val isNewUser: Boolean = false,
)

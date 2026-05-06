package com.lwg.challenge.remote.model.auth

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/auth/kakao 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": { "accessToken": "...", "refreshToken": "...", "userId": 12, "isNewUser": true }
 * }
 * ```
 */
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

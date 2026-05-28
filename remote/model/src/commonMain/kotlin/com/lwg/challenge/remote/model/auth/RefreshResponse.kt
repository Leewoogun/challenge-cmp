package com.lwg.challenge.remote.model.auth

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/auth/refresh 성공 응답.
 *
 * Rotation Refresh Token: 매 refresh 시 access + refresh 모두 재발급 (ADR-0009).
 * code=401 일 경우 refresh 만료 → 로그인 화면으로 이동.
 */
@Serializable
data class RefreshResponse(
    val data: RefreshData = RefreshData(),
) : BaseResponse()

@Serializable
data class RefreshData(
    val accessToken: String = "",
    val refreshToken: String = "",
)

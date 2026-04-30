package com.lwg.challenge.remote.model.auth

import com.lwg.challenge.remote.model.base.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/auth/refresh 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": { "accessToken": "..." }
 * }
 * ```
 *
 * Refresh token 은 재발급하지 않고 기존 값을 재사용. (ADR-0009 예정 시 변경)
 * code=401 일 경우 refresh 만료 → 로그인 화면으로 이동.
 */
@Serializable
data class RefreshResponse(
    val data: RefreshData = RefreshData(),
) : BaseResponse()

@Serializable
data class RefreshData(
    val accessToken: String = "",
)

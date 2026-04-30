package com.lwg.challenge.remote.model.auth

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/auth/refresh 요청 body.
 */
@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

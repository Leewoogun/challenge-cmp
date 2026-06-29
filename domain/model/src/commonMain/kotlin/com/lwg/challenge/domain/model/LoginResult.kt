package com.lwg.challenge.domain.model

/**
 * 카카오 로그인 성공 결과.
 */
data class LoginResult(
    val tokens: AuthTokens,
    val userId: Long,
    val isNewUser: Boolean,
)

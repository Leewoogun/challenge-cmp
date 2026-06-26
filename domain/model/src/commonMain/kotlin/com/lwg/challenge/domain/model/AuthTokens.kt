package com.lwg.challenge.domain.model

/**
 * 인증 토큰 쌍. 서버에서 발급받은 access/refresh token.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
) {
    val isValid: Boolean get() = accessToken.isNotBlank() && refreshToken.isNotBlank()

    companion object {
        val Empty = AuthTokens(accessToken = "", refreshToken = "")
    }
}

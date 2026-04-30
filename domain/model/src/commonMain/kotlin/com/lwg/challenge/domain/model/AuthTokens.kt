package com.lwg.challenge.domain.model

/**
 * 인증 토큰 쌍. 서버에서 발급받은 access/refresh token.
 *
 * [LoginRepository] 를 통해 [SecureTokenStorage] 에 저장/조회된다.
 * access 가 만료되면 refresh 로 재발급을 시도하며, refresh 도 만료면 로그인 화면으로 이동.
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

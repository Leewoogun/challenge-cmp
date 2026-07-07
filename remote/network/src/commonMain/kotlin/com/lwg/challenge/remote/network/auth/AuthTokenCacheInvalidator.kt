package com.lwg.challenge.remote.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

/**
 * Ktor [BearerAuthProvider] 의 토큰 캐시 무효화.
 *
 * Auth 플러그인은 loadTokens 결과를 클라이언트 수명 동안 캐싱하므로,
 * 로그인 성공/로그아웃으로 dataStore 토큰이 바뀌어도 캐시를 비우지 않으면
 * 이후 요청이 옛 토큰(또는 null)으로 나가 401 → sessionExpired 로 이어진다.
 * 토큰이 바뀌는 지점(로그인/로그아웃)에서 반드시 [invalidate] 를 호출한다.
 */
class AuthTokenCacheInvalidator(
    private val httpClient: HttpClient,
) {
    fun invalidate() {
        httpClient.authProviders
            .filterIsInstance<BearerAuthProvider>()
            .firstOrNull()
            ?.clearToken()
    }
}

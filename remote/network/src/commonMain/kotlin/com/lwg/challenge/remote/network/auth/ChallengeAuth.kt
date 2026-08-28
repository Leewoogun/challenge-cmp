package com.lwg.challenge.remote.network.auth

import com.lwg.challenge.remote.model.auth.RefreshRequest
import com.lwg.challenge.remote.model.auth.RefreshResponse
import com.lwg.challenge.utils.AuthEventBus
import com.lwg.challenge.utils.Logger
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val CODE_SUCCESS = 200
private const val CODE_UNAUTHORIZED = 401

/**
 * 토큰을 선제 부착하지 **않는** 경로. 이 목록 밖은 전부 Bearer 필수다.
 *
 * `api/v1/auth/refresh` 가 빠지면 만료된 access 를 갱신 요청에 실어 보내고, 그 요청이 다시 401 →
 * 갱신을 부르는 재귀가 된다. 반드시 남아 있어야 한다.
 */
private val UNAUTHENTICATED_PATHS = setOf(
    "api/v1/auth/kakao",
    "api/v1/auth/refresh",
    "api/v1/auth/test-login",
)

/**
 * [baseUrl] 은 KtorfitModule 에서는 BuildKonfig.BASE_URL 이 넘어온다.
 * 테스트가 MockEngine 주소를 넣을 수 있도록 파라미터로 뺐다.
 */
fun HttpClientConfig<*>.installChallengeAuth(
    tokenProvider: TokenProvider,
    authEventBus: AuthEventBus,
    baseUrl: String,
) {
    install(Auth) {
        bearer {
            loadTokens {
                val access = tokenProvider.getAccessToken()
                val refresh = tokenProvider.getRefreshToken()
                if (access.isNotBlank()) BearerTokens(access, refresh) else null
            }

            refreshTokens {
                val refreshToken = oldTokens?.refreshToken
                if (refreshToken.isNullOrBlank()) {
                    authEventBus.emitSessionExpired()
                    return@refreshTokens null
                }

                try {
                    val response = client.post("${baseUrl}api/v1/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refreshToken))
                    }

                    val body = response.body<RefreshResponse>()
                    when (body.code) {
                        CODE_SUCCESS -> {
                            tokenProvider.updateTokens(body.data.accessToken, body.data.refreshToken)
                            BearerTokens(body.data.accessToken, body.data.refreshToken)
                        }
                        CODE_UNAUTHORIZED -> {
                            Logger.w("Refresh token expired — forcing logout")
                            tokenProvider.clearTokens()
                            authEventBus.emitSessionExpired()
                            null
                        }
                        else -> {
                            Logger.e("Unexpected refresh response code: ${body.code}")
                            null
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("Token refresh failed", e)
                    null
                }
            }

            // 경로 '일부'(auth 세그먼트 포함 여부)로 판정하면 /auth/logout 처럼 auth 밑에 있으면서
            // 인증이 필요한 엔드포인트까지 부착 대상에서 빠진다. 그래서 경로 전체를 allowlist 와 비교한다.
            // 또한 ktor-client-auth 의 refreshTokenIfNeeded 는 토큰이 선제 부착된 요청만 갱신 대상으로
            // 삼으므로, 여기서 빠지면 401 을 받아도 자동 갱신조차 돌지 않는다.
            //
            // allowlist 방향을 고른 이유는 실패 방향이 안전한 쪽이기 때문이다 — 새 공개 엔드포인트를
            // 빠뜨리면 공개 API 에 토큰이 붙을 뿐(무해)이지만, 반대 방식(차단 목록)은 보호된 API 에서
            // 토큰이 빠져 조용히 401 이 된다.
            sendWithoutRequest { request ->
                request.url.pathSegments.filter { it.isNotEmpty() }.joinToString("/") !in UNAUTHENTICATED_PATHS
            }
        }
    }
}

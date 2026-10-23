package com.lwg.challenge.remote.network.auth

import com.lwg.challenge.remote.model.auth.RefreshRequest
import com.lwg.challenge.remote.model.auth.RefreshResponse
import com.lwg.challenge.remote.network.util.CODE_UNAUTHORIZED
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
import io.ktor.http.Url
import io.ktor.http.contentType

private const val CODE_SUCCESS = 200

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
    val apiHost = Url(baseUrl).host

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

            // 같은 클라이언트로 외부 이미지(카카오 프로필 등)도 받으므로 우리 서버 호스트에만 토큰을 붙인다.
            sendWithoutRequest { request ->
                request.url.host == apiHost &&
                    request.url.pathSegments.filter { it.isNotEmpty() }.joinToString("/") !in UNAUTHENTICATED_PATHS
            }
        }
    }
}

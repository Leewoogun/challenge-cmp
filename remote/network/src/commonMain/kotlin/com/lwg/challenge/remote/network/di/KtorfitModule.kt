package com.lwg.challenge.remote.network.di

import com.lwg.challenge.remote.model.auth.RefreshRequest
import com.lwg.challenge.remote.model.auth.RefreshResponse
import com.lwg.challenge.remote.network.BuildKonfig
import com.lwg.challenge.remote.network.auth.TokenProvider
import com.lwg.challenge.remote.network.util.ApiResultConverterFactory
import com.lwg.challenge.remote.network.util.HttpNetworkLogger
import com.lwg.challenge.utils.AuthEventBus
import com.lwg.challenge.utils.Logger
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class KtorfitModule {

    @Single
    fun provideAuthEventBus(): AuthEventBus = AuthEventBus()

    @Single
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    @Single
    fun provideHttpClient(
        json: Json,
        tokenProvider: TokenProvider,
        authEventBus: AuthEventBus,
    ): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(DefaultRequest) {
                header("Content-Type", "application/json; charset=utf-8")
            }

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
                            val response = client.post("${BuildKonfig.BASE_URL}api/v1/auth/refresh") {
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

                    sendWithoutRequest { request ->
                        request.url.pathSegments.none { it == "auth" }
                    }
                }
            }

            install(HttpNetworkLogger)
        }
    }

    @Single
    fun provideKtorfit(httpClient: HttpClient): Ktorfit {
        return Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl(BuildKonfig.BASE_URL)
            .converterFactories(ApiResultConverterFactory())
            .build()
    }

    private companion object {
        const val CODE_SUCCESS = 200
        const val CODE_UNAUTHORIZED = 401
    }
}

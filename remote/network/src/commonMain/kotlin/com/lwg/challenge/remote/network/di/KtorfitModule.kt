package com.lwg.challenge.remote.network.di

import com.lwg.challenge.remote.network.auth.AuthTokenCacheInvalidator
import com.lwg.challenge.remote.network.auth.TokenProvider
import com.lwg.challenge.remote.network.auth.installChallengeAuth
import com.lwg.challenge.remote.network.util.ApiResultConverterFactory
import com.lwg.challenge.remote.network.util.HttpNetworkLogger
import com.lwg.challenge.utils.AuthEventBus
import com.lwg.challenge.utils.Constants
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.util.appendIfNameAbsent
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
                // 요청이 스스로 Content-Type 을 정했으면(multipart 등) 기본값 JSON 을 덧붙이지 않는다
                // — 값이 둘이 되면 어느 쪽이 쓰일지가 순서에 의존한다
                headers.appendIfNameAbsent(HttpHeaders.ContentType, "application/json; charset=utf-8")
            }

            installChallengeAuth(tokenProvider, authEventBus, Constants.BASE_URL)

            install(HttpNetworkLogger)
        }
    }

    @Single
    fun provideAuthTokenCacheInvalidator(httpClient: HttpClient): AuthTokenCacheInvalidator =
        AuthTokenCacheInvalidator(httpClient)

    @Single
    fun provideKtorfit(httpClient: HttpClient): Ktorfit {
        return Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl(Constants.BASE_URL)
            .converterFactories(ApiResultConverterFactory())
            .build()
    }
}

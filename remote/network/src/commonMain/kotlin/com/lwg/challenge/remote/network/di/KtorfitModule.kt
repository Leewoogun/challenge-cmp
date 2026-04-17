package com.lwg.challenge.remote.network.di

import com.lwg.challenge.remote.network.BuildKonfig
import com.lwg.challenge.remote.network.util.ApiResultConverterFactory
import com.lwg.challenge.remote.network.util.HttpNetworkLogger
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class KtorfitModule {

    @Single
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    @Single
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(DefaultRequest) {
                header("Content-Type", "application/json; charset=utf-8")
                header("Authorization", "Bearer ${BuildKonfig.TMDB_TOKEN}")
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
}

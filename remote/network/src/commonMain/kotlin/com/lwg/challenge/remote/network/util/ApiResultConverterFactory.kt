package com.lwg.challenge.remote.network.util

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

/**
 * Ktorfit Converter Factory for ApiResult
 *
 * Retrofit의 CallAdapterFactory와 동일한 역할을 수행합니다.
 * API 인터페이스의 반환 타입이 ApiResult<T>인 경우,
 * HTTP 응답을 자동으로 ApiResult로 래핑합니다.
 */
class ApiResultConverterFactory : Converter.Factory {

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.SuspendResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type != ApiResult::class) {
            return null
        }

        return object : Converter.SuspendResponseConverter<HttpResponse, ApiResult<Any>> {
            override suspend fun convert(result: KtorfitResult): ApiResult<Any> {
                return when (result) {
                    is KtorfitResult.Success -> {
                        val response = result.response
                        try {
                            if (response.status.value in 200..299) {
                                val innerTypeData = typeData.typeArgs.first()
                                val body: Any = response.body(innerTypeData.typeInfo)
                                ApiResult.Success(body)
                            } else {
                                ApiResult.Failure.HttpError(
                                    status_code = response.status.value,
                                    status_message = response.status.description,
                                    success = false,
                                )
                            }
                        } catch (e: Exception) {
                            ApiResult.Failure.UnknownApiError(e)
                        }
                    }

                    is KtorfitResult.Failure -> {
                        val throwable = result.throwable
                        if (throwable is kotlinx.io.IOException) {
                            ApiResult.Failure.NetworkError
                        } else {
                            ApiResult.Failure.UnknownApiError(throwable)
                        }
                    }
                }
            }
        }
    }
}

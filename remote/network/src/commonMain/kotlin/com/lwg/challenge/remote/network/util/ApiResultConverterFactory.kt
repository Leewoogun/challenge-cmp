package com.lwg.challenge.remote.network.util

import com.lwg.challenge.remote.model.BaseResponse
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

/**
 * Ktorfit Converter Factory for ApiResult.
 *
 * 응답 변환 규칙 (ADR-0002):
 * 1. HTTP 4xx/5xx → [ApiResult.Failure.HttpError].
 * 2. HTTP 2xx + `body.code != 200` → [ApiResult.Failure.CustomError] (서버 비즈니스 에러).
 * 3. HTTP 2xx + `body.code == 200` → [ApiResult.Success].
 * 4. IOException → [ApiResult.Failure.NetworkError].
 * 5. 기타 예외 → [ApiResult.Failure.UnknownApiError].
 *
 * `T : BaseResponse` 제약이 ApiResult 시그니처에 걸려있어, 응답 body 는 항상 [BaseResponse] 자손.
 * Repository 는 더 이상 `body.code` 를 직접 검사하지 않는다.
 */
class ApiResultConverterFactory : Converter.Factory {

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.SuspendResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type != ApiResult::class) {
            return null
        }

        return object : Converter.SuspendResponseConverter<HttpResponse, ApiResult<BaseResponse>> {
            override suspend fun convert(result: KtorfitResult): ApiResult<BaseResponse> {
                return when (result) {
                    is KtorfitResult.Success -> {
                        val response = result.response
                        try {
                            if (response.status.value in 200..299) {
                                val innerTypeData = typeData.typeArgs.first()
                                val body = response.body(innerTypeData.typeInfo) as BaseResponse
                                if (body.code != CODE_SUCCESS) {
                                    ApiResult.Failure.CustomError(
                                        code = body.code,
                                        message = body.message,
                                    )
                                } else {
                                    ApiResult.Success(body)
                                }
                            } else {
                                ApiResult.Failure.HttpError(
                                    code = response.status.value,
                                    message = response.status.description,
                                    body = response.bodyAsText(),
                                )
                            }
                        } catch (e: Exception) {
                            ApiResult.Failure.UnknownApiError(e)
                        }
                    }

                    is KtorfitResult.Failure -> {
                        val throwable = result.throwable
                        if (throwable is kotlinx.io.IOException) {
                            ApiResult.Failure.NetworkError(
                                message = throwable.message ?: "네트워크 연결 실패",
                            )
                        } else {
                            ApiResult.Failure.UnknownApiError(throwable)
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val CODE_SUCCESS = 200
    }
}

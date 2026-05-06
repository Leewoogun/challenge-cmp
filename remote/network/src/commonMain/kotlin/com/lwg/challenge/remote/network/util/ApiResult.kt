package com.lwg.challenge.remote.network.util

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.utils.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 모든 서버 API 응답을 [BaseResponse] 상속 타입으로 받는 것을 컴파일 시점에 강제한다 (ADR-0002).
 * 비-envelope 외부 API 가 필요해지면 별도 결과 타입을 두고, 이 채널은 손대지 않는다.
 */
sealed interface ApiResult<out T : BaseResponse> {
    data class Success<T : BaseResponse>(val response: T) : ApiResult<T>

    sealed interface Failure : ApiResult<Nothing> {
        data class HttpError(val code: Int, val message: String, val body: String) : Failure
        data class CustomError(val code: Int, val message: String) : Failure
        data class NetworkError(val message: String) : Failure
        data class UnknownApiError(val throwable: Throwable) : Failure
    }
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <T : BaseResponse> ApiResult<T>.suspendOnSuccess(
    crossinline action: suspend ApiResult.Success<T>.() -> Unit,
): ApiResult<T> {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    if (this is ApiResult.Success) {
        action(this)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T : BaseResponse> ApiResult<T>.suspendOnFailureWithErrorHandling(
    crossinline onError: (String) -> Unit = {},
): ApiResult<T> {
    contract { callsInPlace(onError, InvocationKind.AT_MOST_ONCE) }
    if (this is ApiResult.Failure) {
        when (this) {
            is ApiResult.Failure.HttpError -> {
                Logger.e("HttpError: code=$code, message=$message, body=$body")
                onError(message)
            }

            is ApiResult.Failure.NetworkError -> {
                Logger.e("NetworkError: 네트워크 연결 실패: $message")
                onError("네트워크 오류가 발생하였습니다. 네트워크를 확인하여 주세요.")
            }

            is ApiResult.Failure.UnknownApiError -> {
                Logger.e("UnknownApiError: ${throwable.message}", throwable)
            }

            is ApiResult.Failure.CustomError -> {
                Logger.w("CustomError: code=$code, message=$message")
                onError(message)
            }
        }
    }
    return this
}

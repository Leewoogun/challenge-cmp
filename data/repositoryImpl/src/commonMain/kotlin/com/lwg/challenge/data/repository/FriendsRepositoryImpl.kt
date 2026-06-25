package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.FriendsApiException
import com.lwg.challenge.domain.model.friend.FriendsNetworkException
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.remote.api.FriendsApi
import com.lwg.challenge.remote.mapper.toFriendRequests
import com.lwg.challenge.remote.mapper.toFriends
import com.lwg.challenge.remote.mapper.toUserSearchResults
import com.lwg.challenge.remote.model.friend.SendFriendRequestBody
import com.lwg.challenge.remote.network.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

/**
 * [FriendsRepository] 구현체.
 *
 * 패턴 (HomeRepositoryImpl / RecordRepositoryImpl 동일):
 * - `Flow<T>` 메서드: `ApiResult` 분기 후 Success → mapper → emit, 그 외 Failure → onError(Throwable).
 * - `suspend` 메서드: `ApiResult` 분기 후 Success → `Result.success(Unit)`, 그 외 Failure → `Result.failure(throwable)`.
 *
 * ## 401(세션 만료) 처리 — Ktor Auth 전담
 * 401 은 [com.lwg.challenge.remote.network] 의 Ktor Auth(`refreshTokens`)가 전담한다.
 * 토큰 갱신에 실패하면 그쪽에서 `AuthEventBus.emitSessionExpired()` 로 로그인 화면 전환을 트리거한다.
 * 따라서 이 Repository 는 401 에 대해 [com.lwg.challenge.utils.AuthEventBus] 를 다루지 않고,
 * 혹시 호출자까지 누출된 HTTP 401 은 **조용히 흡수**한다 (Flow 는 무방출, mutation 은 `Result.success`).
 * → ViewModel 은 401 을 전혀 보지 못하므로 중복 스낵바가 발생하지 않는다.
 *
 * ## ViewModel 이 받는 Throwable 종류 (2 종)
 * - [FriendsApiException] — 비즈니스/HTTP 에러 (code 700/702/703/705 등). `message` 를 그대로 사용자에게 노출.
 * - [FriendsNetworkException] — 네트워크/직렬화/Unknown. 고정 문구 노출 권장.
 */
@Single(binds = [FriendsRepository::class])
class FriendsRepositoryImpl(
    private val friendsApi: FriendsApi,
) : FriendsRepository {

    override fun searchUsers(
        nickname: String,
        onError: (Throwable) -> Unit,
    ): Flow<List<UserSearchResult>> = flow {
        when (val result = friendsApi.searchUsers(nickname)) {
            is ApiResult.Success -> emit(result.response.toUserSearchResults())
            is ApiResult.Failure -> handleFailure(result, onError)
        }
    }

    override fun listFriends(
        onError: (Throwable) -> Unit,
    ): Flow<List<Friend>> = flow {
        when (val result = friendsApi.listFriends()) {
            is ApiResult.Success -> emit(result.response.toFriends())
            is ApiResult.Failure -> handleFailure(result, onError)
        }
    }

    override fun listReceivedRequests(
        onError: (Throwable) -> Unit,
    ): Flow<List<FriendRequest>> = flow {
        when (val result = friendsApi.listReceivedRequests()) {
            is ApiResult.Success -> emit(result.response.toFriendRequests())
            is ApiResult.Failure -> handleFailure(result, onError)
        }
    }

    override suspend fun sendRequest(receiverId: Long): Result<Unit> =
        toResult(friendsApi.sendRequest(SendFriendRequestBody(receiverId)))

    override suspend fun acceptRequest(requestId: Long): Result<Unit> =
        toResult(friendsApi.acceptRequest(requestId))

    override suspend fun rejectRequest(requestId: Long): Result<Unit> =
        toResult(friendsApi.rejectRequest(requestId))

    override suspend fun cancelRequest(requestId: Long): Result<Unit> =
        toResult(friendsApi.cancelRequest(requestId))

    /**
     * `Flow<T>` 메서드의 Failure 처리.
     * 401 → 조용히 무시 (emit / onError 모두 안 함). 세션 만료 전환은 Ktor Auth 가 전담.
     * 그 외 → onError(Throwable).
     */
    private fun handleFailure(
        failure: ApiResult.Failure,
        onError: (Throwable) -> Unit,
    ) {
        if (failure.isUnauthorized()) return
        onError(failure.toThrowable())
    }

    /**
     * mutation 메서드의 [ApiResult] → [Result] 변환.
     * Success → `Result.success(Unit)`.
     * 401 → `Result.success(Unit)` 로 조용히 흡수 (ViewModel 은 401 을 모른다 — 세션 만료는 Ktor Auth 전담).
     * 그 외 → `Result.failure(throwable)` — [FriendsApiException] 또는 [FriendsNetworkException].
     */
    private fun toResult(result: ApiResult<*>): Result<Unit> = when (result) {
        is ApiResult.Success -> Result.success(Unit)
        is ApiResult.Failure -> if (result.isUnauthorized()) {
            Result.success(Unit)
        } else {
            Result.failure(result.toThrowable())
        }
    }

    /**
     * 호출자까지 누출된 **실제 HTTP 401** 여부.
     * (envelope `code` 에는 401 이 실리지 않는다 — 인증 실패는 서버가 실제 HTTP 401 로 내려준다.)
     */
    private fun ApiResult.Failure.isUnauthorized(): Boolean =
        this is ApiResult.Failure.HttpError && code == CODE_UNAUTHORIZED

    /**
     * [ApiResult.Failure] → ViewModel 이 보는 2 종 throwable 로 정규화.
     * - HttpError / CustomError → [FriendsApiException]
     * - NetworkError / UnknownApiError → [FriendsNetworkException]
     */
    private fun ApiResult.Failure.toThrowable(): Throwable = when (this) {
        is ApiResult.Failure.HttpError -> FriendsApiException(code, message)
        is ApiResult.Failure.CustomError -> FriendsApiException(code, message)
        is ApiResult.Failure.NetworkError -> FriendsNetworkException(message)
        is ApiResult.Failure.UnknownApiError -> FriendsNetworkException(
            throwable.message ?: "알 수 없는 오류",
        )
    }

    private companion object {
        const val CODE_UNAUTHORIZED = 401
    }
}

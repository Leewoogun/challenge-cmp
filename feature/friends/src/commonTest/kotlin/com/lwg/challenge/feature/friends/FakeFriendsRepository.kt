package com.lwg.challenge.feature.friends

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 테스트용 [FriendsRepository] 페이크.
 *
 * - `listFriends` / `listReceivedRequests` / `searchUsers` 는 StateFlow 백킹 → 외부에서 emit 변경 가능.
 * - `searchInitialError` / 각 mutation 의 `*Result` 로 성공/실패 시나리오 주입.
 * - mutation 호출 시 인자 기록 (호출 검증용).
 */
class FakeFriendsRepository : FriendsRepository {

    val friendsFlow = MutableStateFlow<List<Friend>>(emptyList())
    val receivedRequestsFlow = MutableStateFlow<List<FriendRequest>>(emptyList())
    val searchResultsFlow = MutableStateFlow<List<UserSearchResult>>(emptyList())

    /** searchUsers 호출 시 onError 콜백으로 emit 할 throwable. null 이면 정상 흐름. */
    var searchInitialError: Throwable? = null

    /** 다음 mutation 결과. 기본 success. */
    var sendRequestResult: Result<Unit> = Result.success(Unit)
    var acceptRequestResult: Result<Unit> = Result.success(Unit)
    var rejectRequestResult: Result<Unit> = Result.success(Unit)
    var cancelRequestResult: Result<Unit> = Result.success(Unit)

    /** mutation 인자 기록. */
    val sendRequestCalls = mutableListOf<Long>()
    val acceptRequestCalls = mutableListOf<Long>()
    val rejectRequestCalls = mutableListOf<Long>()
    val cancelRequestCalls = mutableListOf<Long>()

    val searchCalls = mutableListOf<String>()

    override fun searchUsers(
        nickname: String,
        onError: (Throwable) -> Unit,
    ): Flow<List<UserSearchResult>> {
        searchCalls += nickname
        searchInitialError?.let(onError)
        return searchResultsFlow.asStateFlow()
    }

    override fun listFriends(onError: (Throwable) -> Unit): Flow<List<Friend>> =
        friendsFlow.asStateFlow()

    override fun listReceivedRequests(onError: (Throwable) -> Unit): Flow<List<FriendRequest>> =
        receivedRequestsFlow.asStateFlow()

    override suspend fun sendRequest(receiverId: Long): Result<Unit> {
        sendRequestCalls += receiverId
        return sendRequestResult
    }

    override suspend fun acceptRequest(requestId: Long): Result<Unit> {
        acceptRequestCalls += requestId
        return acceptRequestResult
    }

    override suspend fun rejectRequest(requestId: Long): Result<Unit> {
        rejectRequestCalls += requestId
        return rejectRequestResult
    }

    override suspend fun cancelRequest(requestId: Long): Result<Unit> {
        cancelRequestCalls += requestId
        return cancelRequestResult
    }
}

package com.lwg.challenge.feature.friends.list

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
 * - `getFriendList` / `getReceivedRequestList` / `searchUsers` 는 StateFlow 백킹 → 외부에서 emit 변경 가능.
 * - `searchInitialError` / 각 mutation 의 `*Result` 로 성공/실패 시나리오 주입.
 * - mutation 호출 시 인자 기록 (호출 검증용).
 */
class FakeFriendsRepository : FriendsRepository {

    val friendsFlow = MutableStateFlow<List<Friend>>(emptyList())
    val receivedRequestsFlow = MutableStateFlow<List<FriendRequest>>(emptyList())
    val searchResultsFlow = MutableStateFlow<List<UserSearchResult>>(emptyList())

    /** searchUsers 호출 시 onError 콜백으로 emit 할 메시지. null 이면 정상 흐름. */
    var searchInitialError: String? = null

    /** 다음 mutation 에러 메시지. null 이면 성공(onSuccess). */
    var sendRequestError: String? = null
    var acceptRequestError: String? = null
    var rejectRequestError: String? = null
    var cancelRequestError: String? = null

    val sendRequestCalls = mutableListOf<Long>()
    val acceptRequestCalls = mutableListOf<Long>()
    val rejectRequestCalls = mutableListOf<Long>()
    val cancelRequestCalls = mutableListOf<Long>()

    val searchCalls = mutableListOf<String>()

    override fun searchUsers(
        nickname: String,
        onError: (String) -> Unit,
    ): Flow<List<UserSearchResult>> {
        searchCalls += nickname
        searchInitialError?.let(onError)
        return searchResultsFlow.asStateFlow()
    }

    override fun getFriendList(onError: (String) -> Unit): Flow<List<Friend>> =
        friendsFlow.asStateFlow()

    override fun getReceivedRequestList(onError: (String) -> Unit): Flow<List<FriendRequest>> =
        receivedRequestsFlow.asStateFlow()

    override suspend fun sendRequest(
        receiverId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        sendRequestCalls += receiverId
        sendRequestError.let { if (it == null) onSuccess() else onError(it) }
    }

    override suspend fun acceptRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        acceptRequestCalls += requestId
        acceptRequestError.let { if (it == null) onSuccess() else onError(it) }
    }

    override suspend fun rejectRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        rejectRequestCalls += requestId
        rejectRequestError.let { if (it == null) onSuccess() else onError(it) }
    }

    override suspend fun cancelRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        cancelRequestCalls += requestId
        cancelRequestError.let { if (it == null) onSuccess() else onError(it) }
    }
}

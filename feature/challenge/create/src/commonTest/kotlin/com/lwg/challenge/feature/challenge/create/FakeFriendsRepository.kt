package com.lwg.challenge.feature.challenge.create

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * 테스트용 [FriendsRepository] 페이크. 위저드가 쓰는 `listFriends` 만 의미 있게 구현한다.
 *
 * [error] 가 설정되면 실제 구현과 동일하게 `onError` 만 호출하고 **emit 없이 완료되는** Flow 를 돌려준다.
 */
class FakeFriendsRepository : FriendsRepository {

    val friendsFlow = MutableStateFlow<List<Friend>>(emptyList())

    /** listFriends 호출 시 onError 로 넘길 메시지. null 이면 정상 흐름. */
    var error: String? = null

    var callCount: Int = 0
        private set

    override fun listFriends(onError: (String) -> Unit): Flow<List<Friend>> {
        callCount++
        val message = error
        return if (message == null) {
            friendsFlow.asStateFlow()
        } else {
            onError(message)
            emptyFlow()
        }
    }

    override fun searchUsers(
        nickname: String,
        onError: (String) -> Unit,
    ): Flow<List<UserSearchResult>> = emptyFlow()

    override fun listReceivedRequests(onError: (String) -> Unit): Flow<List<FriendRequest>> =
        emptyFlow()

    override suspend fun sendRequest(
        receiverId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun acceptRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun rejectRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun cancelRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit
}

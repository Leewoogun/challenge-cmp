package com.lwg.challenge.feature.challenge.create

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

/**
 * 테스트용 [FriendsRepository] 페이크. 위저드가 쓰는 `getFriendList` 만 의미 있게 구현한다.
 *
 * 실제 구현(`FriendsRemoteDataSourceImpl`)과 같은 **cold flow** 계약을 지킨다 — 호출 시점이 아니라
 * 수집 시점에 [callCount] 가 오르고 [error] 가 평가된다. 이걸 어기면 파이프라인이 상류를 재수집해
 * 갱신하는 구조(triggerStateIn)를 검증할 수 없다.
 *
 * [error] 가 설정되면 `onError` 만 호출하고 **emit 없이 완료되는** Flow 를 돌려준다.
 */
class FakeFriendsRepository : FriendsRepository {

    /** 수집 시점에 읽어 1회 emit 한다. */
    var friends: List<Friend> = emptyList()

    /** getFriendList 수집 시 onError 로 넘길 메시지. null 이면 정상 흐름. */
    var error: String? = null

    var callCount: Int = 0
        private set

    override fun getFriendList(onError: (String) -> Unit): Flow<List<Friend>> = flow {
        callCount++
        val message = error
        if (message == null) {
            emit(friends)
        } else {
            onError(message)
        }
    }

    override fun searchUsers(
        nickname: String,
        onError: (String) -> Unit,
    ): Flow<List<UserSearchResult>> = emptyFlow()

    override fun getReceivedRequestList(onError: (String) -> Unit): Flow<List<FriendRequest>> =
        emptyFlow()

    override suspend fun sendRequest(
        receiverId: Long,
        onSuccess: (requestId: Long) -> Unit,
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

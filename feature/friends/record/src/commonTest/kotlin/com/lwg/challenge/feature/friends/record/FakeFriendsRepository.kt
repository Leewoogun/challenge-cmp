package com.lwg.challenge.feature.friends.record

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendChallengePage
import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

/**
 * 친구 전적 화면용 [FriendsRepository] 페이크.
 *
 * 응답을 큐로 받는 이유: 재시도·다음 쪽은 **호출 순서마다 다른 응답**이 와야 검증이 성립한다.
 * 실패는 emit 없이 onError 만 부른다 — 실제 RemoteDataSource 와 같은 계약이다.
 */
class FakeFriendsRepository : FriendsRepository {

    private sealed interface Outcome<out T> {
        data class Success<T>(val value: T) : Outcome<T>
        data class Failure(val message: String) : Outcome<Nothing>
    }

    private val recordOutcomes = ArrayDeque<Outcome<FriendRecord>>()
    private val pageOutcomes = ArrayDeque<Outcome<FriendChallengePage>>()

    var getFriendRecordCallCount: Int = 0
        private set

    var getFriendChallengesCallCount: Int = 0
        private set

    /** 요청에 실려 나간 nextChallengeId. 서버가 준 값을 그대로 되돌려 보내는지 여기서 본다. */
    val requestedNextChallengeIds = mutableListOf<Long?>()

    fun enqueueRecord(record: FriendRecord) {
        recordOutcomes += Outcome.Success(record)
    }

    fun enqueueRecordFailure(message: String) {
        recordOutcomes += Outcome.Failure(message)
    }

    // isPageEnd 는 서버 불변식(isPageEnd == (nextChallengeId == null))대로 만든다.
    fun enqueuePage(challenges: List<FriendPastChallenge>, nextChallengeId: Long?) {
        pageOutcomes += Outcome.Success(FriendChallengePage(challenges, nextChallengeId, isPageEnd = nextChallengeId == null))
    }

    fun enqueuePageFailure(message: String) {
        pageOutcomes += Outcome.Failure(message)
    }

    override fun getFriendRecord(friendId: Long, onError: (String) -> Unit): Flow<FriendRecord> = flow {
        getFriendRecordCallCount += 1
        when (val outcome = checkNotNull(recordOutcomes.removeFirstOrNull()) { "enqueueRecord 로 응답을 먼저 심어야 한다" }) {
            is Outcome.Success -> emit(outcome.value)
            is Outcome.Failure -> onError(outcome.message)
        }
    }

    override fun getFriendChallenges(
        friendId: Long,
        nextChallengeId: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<FriendChallengePage> = flow {
        getFriendChallengesCallCount += 1
        requestedNextChallengeIds += nextChallengeId
        val outcome = pageOutcomes.removeFirstOrNull()
            ?: Outcome.Success(FriendChallengePage(emptyList(), nextChallengeId = null, isPageEnd = true))
        when (outcome) {
            is Outcome.Success -> emit(outcome.value)
            is Outcome.Failure -> onError(outcome.message)
        }
    }

    override fun searchUsers(nickname: String, onError: (String) -> Unit): Flow<List<UserSearchResult>> = emptyFlow()

    override fun getFriendList(onError: (String) -> Unit): Flow<List<Friend>> = emptyFlow()

    override fun getReceivedRequestList(onError: (String) -> Unit): Flow<List<FriendRequest>> = emptyFlow()

    override suspend fun sendRequest(receiverId: Long, onSuccess: (requestId: Long) -> Unit, onError: (String) -> Unit) = Unit

    override suspend fun acceptRequest(requestId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) = Unit

    override suspend fun rejectRequest(requestId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) = Unit

    override suspend fun cancelRequest(requestId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) = Unit
}

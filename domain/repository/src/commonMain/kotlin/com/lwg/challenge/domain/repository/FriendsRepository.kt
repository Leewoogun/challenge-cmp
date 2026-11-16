package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendChallengePage
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * 친구 시스템 Repository.
 */
interface FriendsRepository {
    fun searchUsers(
        nickname: String,
        onError: (String) -> Unit,
    ): Flow<List<UserSearchResult>>

    fun getFriendList(
        onError: (String) -> Unit,
    ): Flow<List<Friend>>

    fun getReceivedRequestList(
        onError: (String) -> Unit,
    ): Flow<List<FriendRequest>>

    suspend fun sendRequest(
        receiverId: Long,
        onSuccess: (requestId: Long) -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun acceptRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun rejectRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun cancelRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    fun getFriendRecord(
        friendId: Long,
        onError: (String) -> Unit,
    ): Flow<FriendRecord>

    /**
     * @param nextChallengeId 직전 응답의 [FriendChallengePage.nextChallengeId] 를 그대로 넘긴다. null 이면 맨 앞부터
     * @param size 한 번에 받을 개수. `1..50` 밖은 서버가 거부한다
     */
    fun getFriendChallenges(
        friendId: Long,
        nextChallengeId: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<FriendChallengePage>
}

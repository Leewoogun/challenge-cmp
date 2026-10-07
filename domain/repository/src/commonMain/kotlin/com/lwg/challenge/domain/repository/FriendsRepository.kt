package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.friend.Friend
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
        onSuccess: () -> Unit,
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
}

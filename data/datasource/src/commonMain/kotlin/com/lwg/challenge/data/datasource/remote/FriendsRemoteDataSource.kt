package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import kotlinx.coroutines.flow.Flow

interface FriendsRemoteDataSource {

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

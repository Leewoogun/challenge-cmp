package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.remote.api.FriendsApi
import com.lwg.challenge.remote.mapper.toFriendRequests
import com.lwg.challenge.remote.mapper.toFriends
import com.lwg.challenge.remote.mapper.toUserSearchResults
import com.lwg.challenge.remote.model.friend.SendFriendRequestBody
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [FriendsRepository::class])
class FriendsRepositoryImpl(
    private val friendsApi: FriendsApi,
) : FriendsRepository {

    override fun searchUsers(
        nickname: String,
        onError: (String) -> Unit,
    ): Flow<List<UserSearchResult>> = flow {
        friendsApi.searchUsers(nickname)
            .suspendOnSuccess { this@flow.emit(response.toUserSearchResults()) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override fun listFriends(
        onError: (String) -> Unit,
    ): Flow<List<Friend>> = flow {
        friendsApi.listFriends()
            .suspendOnSuccess { this@flow.emit(response.toFriends()) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override fun listReceivedRequests(
        onError: (String) -> Unit,
    ): Flow<List<FriendRequest>> = flow {
        friendsApi.listReceivedRequests()
            .suspendOnSuccess { this@flow.emit(response.toFriendRequests()) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun sendRequest(
        receiverId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsApi.sendRequest(SendFriendRequestBody(receiverId))
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun acceptRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsApi.acceptRequest(requestId)
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun rejectRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsApi.rejectRequest(requestId)
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun cancelRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsApi.cancelRequest(requestId)
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }
}

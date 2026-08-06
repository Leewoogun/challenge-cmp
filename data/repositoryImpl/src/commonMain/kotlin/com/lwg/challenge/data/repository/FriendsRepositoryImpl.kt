package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.FriendsRemoteDataSource
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [FriendsRepository::class])
class FriendsRepositoryImpl(
    private val friendsRemoteDataSource: FriendsRemoteDataSource,
) : FriendsRepository {

    override fun searchUsers(
        nickname: String,
        onError: (String) -> Unit,
    ): Flow<List<UserSearchResult>> = friendsRemoteDataSource.searchUsers(nickname, onError)

    override fun listFriends(
        onError: (String) -> Unit,
    ): Flow<List<Friend>> = friendsRemoteDataSource.listFriends(onError)

    override fun listReceivedRequests(
        onError: (String) -> Unit,
    ): Flow<List<FriendRequest>> = friendsRemoteDataSource.listReceivedRequests(onError)

    override suspend fun sendRequest(
        receiverId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsRemoteDataSource.sendRequest(receiverId, onSuccess, onError)
    }

    override suspend fun acceptRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsRemoteDataSource.acceptRequest(requestId, onSuccess, onError)
    }

    override suspend fun rejectRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsRemoteDataSource.rejectRequest(requestId, onSuccess, onError)
    }

    override suspend fun cancelRequest(
        requestId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        friendsRemoteDataSource.cancelRequest(requestId, onSuccess, onError)
    }
}

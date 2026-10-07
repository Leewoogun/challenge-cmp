package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.friend.FriendsData
import com.lwg.challenge.domain.repository.FriendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFriendsDataUseCase(
    private val friendsRepository: FriendsRepository,
) {
    operator fun invoke(onError: (String) -> Unit): Flow<FriendsData> = combine(
        friendsRepository.getReceivedRequestList(onError),
        friendsRepository.getFriendList(onError),
    ) { receivedRequests, friends ->
        FriendsData(receivedRequests = receivedRequests, friends = friends)
    }
}

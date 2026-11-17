package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.usecase.paging.PaginatedUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PAGE_SIZE = 20

class GetFriendChallengesUseCase(
    private val friendsRepository: FriendsRepository,
) : PaginatedUseCase<Long, FriendPastChallenge>() {

    operator fun invoke(friendId: Long, onError: (String) -> Unit): Flow<FriendChallengesResult> =
        pagingSource.flow(
            key = friendId,
            fetch = { nextChallengeId ->
                friendsRepository.getFriendChallenges(
                    friendId = friendId,
                    nextChallengeId = nextChallengeId,
                    size = PAGE_SIZE,
                    onError = onError,
                )
            },
            getItems = { it.challenges },
            getNextKey = { it.nextChallengeId },
            getIsPageEnd = { it.isPageEnd },
            keySelector = { it.challengeId },
        ).map { pagingResult ->
            FriendChallengesResult(
                challenges = pagingResult.items,
                isPageEnd = pagingResult.isPageEnd,
            )
        }
}

data class FriendChallengesResult(
    val challenges: List<FriendPastChallenge>,
    val isPageEnd: Boolean,
)

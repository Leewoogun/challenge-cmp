package com.lwg.challenge.feature.friends.list.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest

/** 친구 탭 UI 상태. 받은 요청과 친구가 모두 없으면 [Empty]. */
@Stable
sealed interface FriendsUiState {

    @Immutable
    data object Loading : FriendsUiState

    @Immutable
    data object Empty : FriendsUiState

    @Immutable
    data class Data(
        val receivedRequests: List<FriendRequest>,
        val friends: List<Friend>,
    ) : FriendsUiState {

        val hasReceivedRequests: Boolean get() = receivedRequests.isNotEmpty()

        val hasFriends: Boolean get() = friends.isNotEmpty()
    }
}

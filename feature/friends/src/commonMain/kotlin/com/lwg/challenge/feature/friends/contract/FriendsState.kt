package com.lwg.challenge.feature.friends.contract

import androidx.compose.runtime.Immutable

/**
 * 친구 탭 UI 상태 — 받은 요청 + 친구 목록.
 *
 * `FriendsRepository.listReceivedRequests` + `listFriends` 두 Flow 를 combine 한 단일 상태.
 *
 * spec: docs/features/friends/spec-friend-add.md §6.5
 */
@Immutable
data class FriendsUiState(
    val receivedRequests: List<FriendRequestItemState>,
    val friends: List<FriendItemState>,
) {

    val isEmpty: Boolean get() = receivedRequests.isEmpty() && friends.isEmpty()
}

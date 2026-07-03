package com.lwg.challenge.feature.friends.list.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 친구 탭 UI 상태 — 받은 요청 + 친구 목록.
 *
 * - [Loading] : 첫 로드 중 (combine 미 emit 포함).
 * - [Empty]   : 받은 요청 0건 + 친구 0명.
 * - [Data]    : 받은 요청 또는 친구가 1건 이상.
 *
 * spec: docs/features/friends/spec-friend-add.md §6.5
 */
@Stable
sealed interface FriendsUiState {

    @Immutable
    data object Loading : FriendsUiState

    @Immutable
    data object Empty : FriendsUiState

    @Immutable
    data class Data(
        val receivedRequests: List<FriendRequestItemState>,
        val friends: List<FriendItemState>,
    ) : FriendsUiState
}

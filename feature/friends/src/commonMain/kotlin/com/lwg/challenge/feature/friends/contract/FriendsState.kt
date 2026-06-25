package com.lwg.challenge.feature.friends.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 친구 탭 UI 상태.
 *
 * 1차 1단계 — 백엔드 호출 0건 (Data = object).
 * 1차 2단계 — `FriendsRepository.listReceivedRequests` + `listFriends` 결합.
 *   진입 시 Loading, 둘 다 emit 되면 [Data] 로 전이.
 *
 * spec: docs/features/friends/spec-friend-add.md §6.5
 */
@Stable
sealed interface FriendsUiState {

    @Immutable
    data object Loading : FriendsUiState

    @Immutable
    data class Data(
        val receivedRequests: List<FriendRequestItemState>,
        val friends: List<FriendItemState>,
    ) : FriendsUiState {

        /** 받은 요청 + 친구 모두 0건 → 빈 상태 UI 노출 분기용. */
        val isEmpty: Boolean get() = receivedRequests.isEmpty() && friends.isEmpty()
    }
}

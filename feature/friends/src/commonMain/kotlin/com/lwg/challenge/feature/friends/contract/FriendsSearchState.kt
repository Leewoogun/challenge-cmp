package com.lwg.challenge.feature.friends.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 친구 검색 화면 UI 상태.
 *
 * - [Idle]      : 입력 < 2자 (초기 진입 포함). 안내 문구 노출.
 * - [Searching] : debounce 후 API 호출 중.
 * - [Result]    : 결과 ≥ 1건.
 * - [Empty]     : 입력 ≥ 2자 인데 결과 0건.
 *
 * spec: docs/features/friends/spec-friend-add.md §6.5
 */
@Stable
sealed interface FriendsSearchUiState {

    @Immutable
    data object Idle : FriendsSearchUiState

    @Immutable
    data object Searching : FriendsSearchUiState

    @Immutable
    data class Result(val items: List<FriendSearchItemState>) : FriendsSearchUiState

    @Immutable
    data object Empty : FriendsSearchUiState
}

package com.lwg.challenge.feature.friends.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 친구 탭 UI 상태.
 *
 * 1차 1단계 — 백엔드 호출 0건. 진입과 동시에 [Data].
 * 1차 2단계 도입 시 [Data] 를 `data class Data(val friends: List<Friend>)` 로 확장.
 */
@Stable
sealed interface FriendsUiState {

    @Immutable
    data object Loading : FriendsUiState

    @Immutable
    data object Data : FriendsUiState
}

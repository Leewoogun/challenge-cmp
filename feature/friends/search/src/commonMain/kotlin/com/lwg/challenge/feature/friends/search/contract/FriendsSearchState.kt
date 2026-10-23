package com.lwg.challenge.feature.friends.search.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.friend.UserSearchResult

/** 친구 검색 UI 상태. 입력이 2자 미만이면 [Idle], 2자 이상인데 결과가 없으면 [Empty]. */
@Stable
sealed interface FriendsSearchUiState {

    @Immutable
    data object Idle : FriendsSearchUiState

    @Immutable
    data object Searching : FriendsSearchUiState

    /** @property requestingUserIds 친구 요청·수락·취소 응답을 기다리는 사용자. 이 행들은 버튼이 잠긴다. */
    @Immutable
    data class Result(
        val items: List<UserSearchResult>,
        val requestingUserIds: Set<Long>,
    ) : FriendsSearchUiState {

        fun isActionEnabled(item: UserSearchResult): Boolean = item.userId !in requestingUserIds
    }

    @Immutable
    data object Empty : FriendsSearchUiState
}

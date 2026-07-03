package com.lwg.challenge.feature.friends.search.contract

import androidx.compose.runtime.Immutable
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserSearchResult

/**
 * 검색 결과 1행 UI State.
 *
 * relation 별 액션 라벨 / 활성화 / 취소 버튼 노출 여부를 `get()` 으로 캡슐화.
 * (UI State 파싱 규칙 — challenge-app/CLAUDE.md 104-114)
 */
@Immutable
data class FriendSearchItemState(
    internal val model: UserSearchResult
) {

    val userId: Long get() = model.userId
    val nickname: String get() = model.nickname
    val profileImageUrl: String? get() = model.profileImageUrl
    val relation: Relation get() = model.relation
    val pendingRequestId: Long? get() = model.pendingRequestId

    val actionLabel: String
        get() = when (model.relation) {
            Relation.NONE -> "친구 요청"
            Relation.REQUEST_SENT -> "요청 보냄"
            Relation.REQUEST_RECEIVED -> "수락"
            Relation.FRIEND -> "이미 친구"
            Relation.REJECTED -> "다시 요청"
        }

    val isActionEnabled: Boolean get() = model.relation != Relation.FRIEND

    val showCancel: Boolean get() = model.relation == Relation.REQUEST_SENT
}

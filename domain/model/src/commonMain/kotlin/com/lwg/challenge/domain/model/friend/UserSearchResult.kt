package com.lwg.challenge.domain.model.friend

/**
 * 닉네임 검색 결과 1행.
 *
 * 검색 결과 화면에서 [relation] 분기로 액션 버튼을 노출한다.
 * [pendingRequestId] 는 [Relation.REQUEST_SENT] / [Relation.REQUEST_RECEIVED] 일 때만 non-null —
 * 각각 취소 / 수락 호출의 path 로 사용.
 *
 * api-contract: docs/features/friends/api-contract-friend-add.md §1
 *
 * @property userId target user id (= 검색된 사용자)
 * @property nickname target 닉네임
 * @property profileImageUrl null = 미설정
 * @property relation 현재 사용자(me) ↔ target 관계
 * @property pendingRequestId PENDING row id (REQUEST_SENT / REQUEST_RECEIVED 일 때만)
 */
data class UserSearchResult(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val relation: Relation,
    val pendingRequestId: Long?,
)

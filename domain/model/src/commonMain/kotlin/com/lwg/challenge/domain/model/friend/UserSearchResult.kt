package com.lwg.challenge.domain.model.friend

/**
 * 닉네임 검색 결과 1행.
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

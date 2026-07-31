package com.lwg.challenge.domain.model.friend

import kotlinx.datetime.LocalDateTime

/**
 * 친구 목록 1행 (ACCEPTED).
 *
 * @property userId 상대(나 아닌) user id — 백엔드가 "내가 아닌 쪽"을 매핑.
 * @property nickname 상대 닉네임
 * @property profileImageUrl null = 미설정
 * @property since 친구가 된 시각 (friendships.accepted_at, KST). 표시는 UI 에서 변환. 파싱 실패 시 null
 */
data class Friend(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val since: LocalDateTime?,
)

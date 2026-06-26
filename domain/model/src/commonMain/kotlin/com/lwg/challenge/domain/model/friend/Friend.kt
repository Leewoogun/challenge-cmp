@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.domain.model.friend

import kotlin.time.Instant

/**
 * 친구 목록 1행 (ACCEPTED).
 *
 * @property userId 상대(나 아닌) user id — 백엔드가 "내가 아닌 쪽"을 매핑.
 * @property nickname 상대 닉네임
 * @property profileImageUrl null = 미설정
 * @property since 친구가 된 시각 (friendships.accepted_at). 표시는 UI 에서 변환.
 */
data class Friend(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val since: Instant,
)
